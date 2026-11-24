package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.notAutomatable
import dev.slne.surf.player.tests.dsl.scenario
import dev.slne.surf.player.tests.infra.Yaml

private fun Ctx.playtimeText(bot: Bot, command: String): String {
    val m = bot.run(command)
    bot.expect("Spielzeit", since = m, timeout = 8000)
    sleep(600)
    return bot.messagesSince(m).joinToString("\n") { it.plain }
}

/** (current_login_streak, highest_login_streak, last_login_date) */
private fun Ctx.streakRow(bot: Bot) = net.sql("""SELECT current_login_streak, highest_login_streak, last_login_date FROM "surf-playtime".playtime_streaks WHERE player_uuid = '${bot.uuid}'""").firstOrNull()

/** Re-joins the network so the login streak is evaluated, returns the streak message (if any). */
private fun Ctx.rejoinForStreak(bot: Bot): String? {
    quit(bot)
    join(bot)
    return bot.await(5000, 0) { it.plain.contains("Streak") }?.plain?.trim()
}

fun registerPlaytime() {
    scenario("playtime-01", "/playtime") {
        on(PAPER, p1)
        val t = playtimeText(p1, "playtime")
        val missing = listOf("Deine Spielzeit", "Gesamt", "Login-Streak").filter { !t.contains(it) }
        check(missing.isEmpty()) { "Fehlt: $missing in „${t.take(300)}“" }
        note("„${t.trim().replace("\n", " ⏎ ").take(300)}“")
    }
    scenario("playtime-02", "/pt Alias") {
        on(PAPER, p1)
        val t = playtimeText(p1, "pt")
        check(t.contains("Deine Spielzeit")) { "„${t.take(200)}“" }
        note("/pt zeigt „Deine Spielzeit“")
    }
    scenario("playtime-03", "/playtime <Spieler>") {
        on(PAPER, admin, p1)
        val t = playtimeText(admin, "playtime ${P1}")
        check(t.contains("Spielzeit von ${P1}")) { "„${t.take(200)}“" }
        note("„${t.trim().replace("\n", " ⏎ ").take(250)}“")
    }
    scenario("playtime-04", "/playtime <Spieler> unbekannt") {
        on(PAPER, admin)
        val m = admin.run("playtime NichtExistent123")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("Der Spieler wurde nicht gefunden.")) { "„${r.plain.trim()}“" }
        if (!hasPrefix(r, dev.slne.surf.player.tests.dsl.Prefix.Error)) odd("„${r.plain.trim()}“ ohne Fehler-Präfix")
        note("„${r.plain.trim()}“")
    }
    scenario("playtime-36", "/surfplayer playtime Aktionsliste") { on(PAPER, admin); note(helpContains(admin, "surfplayer playtime", "reload", "recalculate", "pause")) }
    scenario("playtime-37", "/playtime <Spieler> mit Perk") {
        on(PAPER, p1)
        withPermission(p1, "surf.player.playtime.others") {
            on(LOBBY, p1); on(PAPER, p1)
            note(playtimeText(p1, "playtime ${A}").let { t -> if (!t.contains("Spielzeit von ${A}")) bad("„${t.take(200)}“"); "„Spielzeit von DevAdmin“ mit Perk" })
        }
    }
    scenario("playtime-38", "/playtime aus der Konsole") { note("Paper-Konsole: „${consoleRejects(PAPER, "playtime")}“") }
    scenario("playtime-05", "/playtime <Spieler> ohne Permission") {
        on(PAPER, p1)
        val m = p1.run("playtime ${A}")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(!r.plain.contains("Spielzeit von ${A}")) { "${P1} sieht fremde Spielzeit" }
        note("„${r.plain.trim().replace("\n", " ")}“")
    }
    scenario("playtime-06", "Formatierung") {
        on(PAPER, p1)
        val t = playtimeText(p1, "playtime")
        val total = Regex("Gesamt: ([^\n]+)").find(t)?.groupValues?.get(1) ?: bad("Keine Gesamtzeit in „${t.take(200)}“")
        check(total.matches(Regex("""\d+s|\d+m \d{2}s|\d+h \d{2}m \d{2}s"""))) { "Format „$total“" }
        note("Gesamt „$total“ im Format Xs/Xm YYs/Xh YYm ZZs")
    }
    scenario("playtime-07", "Erste Streak") {
        on(LOBBY, p2)
        net.sql("""DELETE FROM "surf-playtime".playtime_streaks WHERE player_uuid = '${p2.uuid}'""")
        val msg = rejoinForStreak(p2) ?: bad("Keine Streak-Nachricht beim Login")
        val row = streakRow(p2) ?: bad("Kein playtime_streaks-Eintrag")
        check(msg.contains("neu erstellt")) { "„$msg“" }
        note("„$msg“; DB: $row")
    }
    scenario("playtime-08", "Streak gleicher Tag") {
        val before = streakRow(p2)
        val msg = rejoinForStreak(p2)
        val after = streakRow(p2)
        check(before?.getOrNull(0) == after?.getOrNull(0)) { "Streak geändert: $before → $after" }
        note("Streak unverändert ($after); Nachricht: ${msg ?: "keine"}")
    }
    scenario("playtime-09", "Streak Folgetag") {
        val cols = net.sql("""SELECT column_name FROM information_schema.columns WHERE table_schema = 'surf-playtime' AND table_name = 'playtime_streaks'""").map { it[0] }
        val dateCol = cols.firstOrNull { it.contains("last") } ?: skip("Spalten: $cols")
        net.sql("""UPDATE "surf-playtime".playtime_streaks SET $dateCol = $dateCol - interval '1 day' WHERE player_uuid = '${p2.uuid}'""")
        val before = streakRow(p2)
        val msg = rejoinForStreak(p2)
        val after = streakRow(p2)
        note("vorher $before, nachher $after; Nachricht: ${msg ?: "keine"}")
        if (before == after) bad("Streak nach Login am Folgetag unverändert: $after")
    }
    scenario("playtime-10", "Streak Lücke") {
        val cols = net.sql("""SELECT column_name FROM information_schema.columns WHERE table_schema = 'surf-playtime' AND table_name = 'playtime_streaks'""").map { it[0] }
        val dateCol = cols.firstOrNull { it.contains("last") } ?: skip("Spalten: $cols")
        net.sql("""UPDATE "surf-playtime".playtime_streaks SET $dateCol = $dateCol - interval '3 day' WHERE player_uuid = '${p2.uuid}'""")
        val msg = rejoinForStreak(p2)
        val after = streakRow(p2)
        note("nach 3 Tagen Lücke: $after; Nachricht: ${msg ?: "keine"}")
    }
    notAutomatable("playtime-11", "Streak-Pause über die Lücke: Ergebnis hängt von gespeicherten Sessions vergangener Tage ab, die sich nicht ohne Eingriff in Fremdlogik fälschen lassen; Pausen-Befehle siehe playtime-30..34.")
    scenario("playtime-12", "AFK nach 3 Minuten") {
        on(PAPER, p1)
        p1.look(10f, 0f)
        val m = p1.mark()
        val r = p1.expect("AFK", since = m, timeout = 200_000)
        sleep(1500)
        val tabAfk = p1.tab[p1.uuid]?.display ?: ""
        note("Nach ~3 min: „${r.plain.trim()}“; Tab: „$tabAfk“")
        if (!tabAfk.contains("AFK")) odd("[AFK] fehlt im eigenen Tab-Eintrag; ${notes.last()}")
    }
    scenario("playtime-13", "AFK beenden") {
        // Self-contained: the bot must be AFK first. It used to rely on playtime-12 having made the
        // same bot AFK, which does not hold when the two run in different bot groups.
        on(PAPER, p1)
        p1.look(20f, 0f)
        val m0 = p1.mark()
        p1.expect("Du bist nun AFK", since = m0, timeout = 200_000)
        val m = p1.mark()
        p1.look(90f, 10f)
        val r = p1.expect("nicht mehr AFK", since = m, timeout = 6000)
        note("„${r.plain.trim()}“")
    }
    notAutomatable("playtime-14", "Laufen ohne Mausbewegung: der Bot kann sich wegen Paper-26.3-Bewegungsprüfung (\"moved too quickly\") nicht frei bewegen; nur Blickrichtung wird gesendet.")
    notAutomatable("playtime-15", "Braucht 5+ Minuten AFK und Vergleich der Sessions; zu langsam für den Lauf, Session-Logik durch Unit-Tests der Playtime-Sessions abgedeckt.")
    scenario("playtime-16", "Session-Speicherung") {
        on(PAPER, p2)
        sleep(3000)
        val before = net.sql("""SELECT count(*) FROM "surf-playtime".playtime_sessions WHERE playtime_player_id = '${p2.uuid}'""")[0][0].toInt()
        on(LOBBY, p2)
        sleep(3000)
        val rows = net.sql("""SELECT count(*) FROM "surf-playtime".playtime_sessions WHERE playtime_player_id = '${p2.uuid}'""")[0][0].toInt()
        val last = net.sql("""SELECT server_name, server_category, start_time, end_time FROM "surf-playtime".playtime_sessions WHERE playtime_player_id = '${p2.uuid}' ORDER BY id DESC LIMIT 1""").firstOrNull()
        check(rows > before) { "Keine neue Session ($before → $rows)" }
        note("Sessions $before → $rows; letzte: $last")
    }
    notAutomatable("playtime-17", "Periodischer Flush alle 5 Minuten; nicht im Lauf abgewartet.")
    notAutomatable("playtime-18", "PayCheck braucht surf-transaction (nicht im Testnetz installiert).")
    notAutomatable("playtime-19", "PayCheck maxBalance braucht surf-transaction.")
    notAutomatable("playtime-20", "PayCheck braucht surf-transaction.")
    notAutomatable("playtime-21", "PayCheck braucht surf-transaction.")
    scenario("playtime-22", "PayCheck ohne surf-transaction") {
        on(PAPER, p1)
        withConfig(PAPER, "playtime/config.yml", { Yaml.set(Yaml.set(it, "paycheck.enabled", "true"), "paycheck.interval-minutes", "1") }, { reloadOn(PAPER, "playtime") }) {
            val since = java.time.Instant.now()
            p1.look(30f, 0f)
            sleep(70_000)
            p1.look(60f, 0f)
            val log = net.logsSince("paper", since)
            val line = log.lines().firstOrNull { it.contains("surf-transaction is not available") }
            check(line != null) { "Keine SEVERE-Meldung „surf-transaction is not available“ nach 70 s" }
            note("Log: ${line!!.trim()}")
        }
    }
    notAutomatable("playtime-23", "Event-Coin-Payout braucht surf-transaction.")
    notAutomatable("playtime-24", "Event-Coin-Payout braucht surf-transaction.")
    notAutomatable("playtime-25", "Event-Coin-Währung wird über surf-transaction aufgelöst, das im Testnetz fehlt.")
    scenario("playtime-26", "/surfplayer playtime reload") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime reload")
        note("„${admin.expect("Die Konfiguration von playtime wurde neu geladen", since = m).plain.trim()}“")
    }
    scenario("playtime-27", "/surfplayer playtime recalculate") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime recalculate")
        admin.expect("werden neu berechnet", since = m)
        val r = admin.expect("wurden neu berechnet", since = m, timeout = 20000)
        note("„${r.plain.trim()}“")
    }
    scenario("playtime-28", "/surfplayer playtime recalculate <Spieler>") {
        on(PAPER, admin, p1)
        val m = admin.run("surfplayer playtime recalculate ${P1}")
        val r = admin.expect("Die Login-Streak von ${P1}", since = m, timeout = 10000)
        note("„${r.plain.trim()}“")
    }
    scenario("playtime-29", "recalculate unbekannt / ohne Sessions") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime recalculate NichtExistent123")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht gefunden")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }
    scenario("playtime-30", "pause create") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime pause create 24.12.2030 2030-12-26")
        val r = admin.expect("Die Streak-Pause", "24.12.2030", "26.12.2030", since = m)
        val id = Regex("#(\\d+)").find(r.plain)?.groupValues?.get(1)
        val db = net.sql("""SELECT count(*) FROM "surf-playtime".playtime_streak_pauses""")
        if (id != null) admin.cmd("surfplayer playtime pause delete $id")
        note("„${r.plain.trim()}“; DB-Pausen: ${db[0][0]}")
    }
    scenario("playtime-31", "pause ungültiges Datum") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime pause create 32.13.2030 01.01.2031")
        note("„${admin.expect("Ungültiges Datum", since = m).plain.trim()}“")
    }
    scenario("playtime-32", "pause Ende vor Start") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime pause create 10.01.2031 01.01.2031")
        note("„${admin.expect("Enddatum darf nicht vor dem Startdatum", since = m).plain.trim()}“")
    }
    scenario("playtime-33", "pause list") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime pause list")
        val r = admin.await(6000, m) { it.plain.contains("Streak-Pausen") } ?: bad("Keine Liste: ${observed(admin, m)}")
        note("„${r.plain.trim().replace("\n", " ⏎ ").take(200)}“")
    }
    scenario("playtime-34", "pause delete") {
        on(PAPER, admin)
        val m = admin.run("surfplayer playtime pause create 01.02.2031 02.02.2031")
        val r = admin.expect("Die Streak-Pause", since = m)
        val id = Regex("#(\\d+)").find(r.plain)?.groupValues?.get(1) ?: bad("Keine ID in „${r.plain}“")
        val m2 = admin.run("surfplayer playtime pause delete $id")
        val d = admin.expect("#$id", "gelöscht", since = m2)
        val m3 = admin.run("surfplayer playtime pause delete 999999")
        val n = admin.expect("999999", since = m3)
        note("„${d.plain.trim()}“; „${n.plain.trim()}“")
    }
    scenario("playtime-35", "/surfplayer playtime ohne Permission") {
        on(PAPER, p1)
        p1.expectCommandUnavailable("surfplayer")
        note("/surfplayer fehlt bei ${P1}")
    }
}
