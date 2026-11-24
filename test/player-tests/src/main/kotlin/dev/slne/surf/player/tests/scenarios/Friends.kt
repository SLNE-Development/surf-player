package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.bot.Text
import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.Prefix
import dev.slne.surf.player.tests.dsl.scenario

/** [a] and [b] become friends (from a clean state). */
fun Ctx.makeFriends(a: Bot, b: Bot) {
    resetFriends()
    val m = b.mark()
    a.cmd("friend add ${b.name}")
    b.expect("Freundschaftsanfrage von ${a.name}", since = m, timeout = 8000)
    val m2 = b.run("friend accept ${a.name}")
    b.expect("angenommen", since = m2, timeout = 8000)
    sleep(500)
}

private fun Ctx.request(from: Bot, to: Bot) {
    val m = to.mark()
    from.cmd("friend add ${to.name}")
    to.expect("Freundschaftsanfrage von ${from.name}", since = m, timeout = 8000)
}

/** Friends feature: ${P1} = FPNS, ${A} = NotAmmo. */
fun registerFriends() {
    scenario("friends-01", "/friend add") {
        on(PAPER, admin, p1)
        resetFriends()
        val ma = admin.mark()
        val m = p1.run("friend add ${A}")
        val out = p1.expect("Du hast eine Freundschaftsanfrage an ${A} gesendet", since = m, prefix = Prefix.Success)
        check(dev.slne.surf.player.tests.bot.Text.has(out.plain, "Zurückziehen") && out.clicks.any { it.value.contains("friend revoke") }) { "Kein [Zurückziehen]: „${out.plain}“" }
        val inc = admin.expect("Du hast eine Freundschaftsanfrage von ${P1} erhalten", since = ma)
        check(inc.plain.contains("✔") && inc.plain.contains("❌")) { "Buttons fehlen: „${inc.plain}“" }
        val hovers = inc.hovers.map { it.second }
        check(hovers.any { it.contains("anzunehmen") } && hovers.any { it.contains("abzulehnen") }) { "Hovers: $hovers" }
        sleep(800)
        val sound = admin.since(ma).firstOrNull { it.kind == dev.slne.surf.player.tests.bot.Received.Kind.SOUND }
        note("${P1}: „${out.plain.trim()}“; ${A}: „${inc.plain.trim()}“ mit Hovers annehmen/ablehnen; Sound ${sound?.extra ?: "keiner"}")
        if (sound == null || !sound.extra.contains("pling")) bad("Kein Pling-Sound beim Empfänger; ${notes.last()}")
        resetFriends()
    }

    // Changed on purpose: /friend has no short forms any more (docs/command-conventions.md).
    scenario("friends-02", "/fa entfernt") {
        on(PAPER, admin, p1)
        resetFriends()
        p1.expectCommandUnavailable("fa")
        val ma = admin.mark()
        val m = p1.run("fa ${A}")
        val r = p1.firstMessage(m, 3000) ?: bad("Keine Antwort auf /fa")
        check(r.plain.contains("command.unknown.command") || r.plain.contains("Unbekannt", ignoreCase = true)) { "„${r.plain.trim()}“" }
        admin.expectNone("Freundschaftsanfrage von ${P1}", since = ma, within = 1500)
        note("/fa ist unbekannt: „${r.plain.trim().replace("\n", " ⏎ ")}“, keine Anfrage")
        resetFriends()
    }

    scenario("friends-04", "add: Spieler nicht gefunden") {
        on(PAPER, p1)
        val m = p1.run("friend add NichtExistent123")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht gefunden")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("friends-05", "add: an sich selbst") {
        on(PAPER, p1)
        val m = p1.run("friend add ${P1}")
        val r = p1.expect("keine Freundschaftsanfrage selbst senden", since = m, prefix = Prefix.Error)
        note("„${r.plain.trim()}“")
    }

    scenario("friends-06", "add: bereits gesendet") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        val m = p1.run("friend add ${A}")
        val r = p1.expect("bereits eine Freundschaftsanfrage an ${A}", since = m, prefix = Prefix.Error)
        note("„${r.plain.trim()}“")
        resetFriends()
    }

    scenario("friends-07", "add: bereits erhalten") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        val m = admin.run("friend add ${P1}")
        val r = admin.expect("bereits eine Freundschaftsanfrage von ${P1}", since = m)
        val click = r.clicks.firstOrNull { dev.slne.surf.player.tests.bot.Text.has(it.text, "Akzeptieren") } ?: bad("Kein [Akzeptieren]: ${r.clicks.map { it.text }}")
        check(click.value.contains("friend accept ${P1}")) { "Klick: ${click.value}" }
        val m2 = admin.mark()
        admin.click(click.event)
        admin.expect("angenommen", since = m2)
        note("„${r.plain.trim()}“; Klick führt ${click.value} aus → Freundschaft")
        resetFriends()
    }

    scenario("friends-08", "add: bereits befreundet") {
        on(PAPER, admin, p1)
        makeFriends(p1, admin)
        val m = p1.run("friend add ${A}")
        val r = p1.expect("bereits mit ${A}", "befreundet", since = m, prefix = Prefix.Error)
        note("„${r.plain.trim()}“")
        resetFriends()
    }

    scenario("friends-09", "add: Cooldown nach Ablehnung") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        admin.cmd("friend deny ${P1}"); sleep(1500)
        val m = p1.run("friend add ${A}")
        val r = p1.expect("erst in", since = m, prefix = Prefix.Error)
        val row = net.sql("""SELECT round(extract(epoch from (expires_at - (now() at time zone 'utc')))) FROM "surf-friends".friend_request_cooldowns WHERE sender_uuid = '${p1.uuid}' AND receiver_uuid = '${admin.uuid}'""")
        note("„${r.plain.trim()}“; DB-Cooldown läuft noch ${row.firstOrNull()?.firstOrNull()}s (Testnetz: SURF_PLAYER_FRIEND_REQUEST_DECLINE_COOLDOWN)")
        resetFriends()
    }

    scenario("friends-10", "add: Cooldown nach Zurückziehen") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        p1.cmd("friend revoke ${A}"); sleep(1500)
        val m = p1.run("friend add ${A}")
        val r = p1.expect("erst in", since = m, prefix = Prefix.Error)
        note("„${r.plain.trim()}“ (Testnetz: SURF_PLAYER_FRIEND_REQUEST_WITHDRAW_COOLDOWN)")
        resetFriends()
    }

    scenario("friends-11", "Cooldown-Eskalation") {
        on(PAPER, admin, p1)
        resetFriends()
        val seen = mutableListOf<String>()
        repeat(3) { round ->
            net.sql("""UPDATE "surf-friends".friend_request_cooldowns SET expires_at = (now() at time zone 'utc') - interval '1 second' WHERE sender_uuid = '${p1.uuid}'""")
            request(p1, admin)
            p1.cmd("friend revoke ${A}"); sleep(1500)
            val m = p1.run("friend add ${A}")
            val r = p1.expect("erst in", since = m)
            seen += Regex("erst in (.+?) wieder").find(r.plain)?.groupValues?.get(1) ?: r.plain.trim()
        }
        val secs = net.sql("""SELECT withdraw_count, round(extract(epoch from (expires_at - last_withdraw_at))) FROM "surf-friends".friend_request_cooldowns WHERE sender_uuid = '${p1.uuid}' AND receiver_uuid = '${admin.uuid}'""")
        note("Cooldowns nach 1./2./3. Rücknahme: ${seen.joinToString(" → ")}; DB (Anzahl, Sekunden): ${secs.firstOrNull()}")
        if (seen.distinct().size < 3) bad("Keine Verdopplung: ${seen.joinToString(" → ")}")
        resetFriends()
    }

    scenario("friends-12", "Cooldown nur in eine Richtung") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        p1.cmd("friend revoke ${A}"); sleep(1500)
        val mp = p1.mark()
        val m = admin.run("friend add ${P1}")
        admin.expect("Freundschaftsanfrage an ${P1} gesendet", since = m)
        p1.expect("Freundschaftsanfrage von ${A}", since = mp)
        note("${A} kann trotz Cooldown von ${P1} senden")
        resetFriends()
    }

    scenario("friends-13", "add: blockiert durch Ignore") {
        on(PAPER, admin, p1)
        resetFriends(); resetIgnores(admin)
        admin.cmd("ignore add ${P1}"); sleep(1500)
        try {
            val m = p1.run("friend add ${A}")
            val r1 = p1.firstMessage(m) ?: bad("Keine Antwort für ${P1}")
            val m2 = admin.run("friend add ${P1}")
            val r2 = admin.firstMessage(m2) ?: bad("Keine Antwort für ${A}")
            // Changed on purpose: a request blocked by an ignore reads like disabled requests.
            check(
                r1.plain.contains("${A} nimmt keine Freundschaftsanfragen an.") &&
                    r2.plain.contains("${P1} nimmt keine Freundschaftsanfragen an.")
            ) { "${P1}: „${r1.plain.trim()}“, ${A}: „${r2.plain.trim()}“" }
            check(r1.component?.let(Text::prefixColor) == Prefix.Error.color && r2.component?.let(Text::prefixColor) == Prefix.Error.color) {
                "Kein Fehler-Präfix: „${r1.plain.trim()}“ / „${r2.plain.trim()}“"
            }
            note("Beide Richtungen: „${r1.plain.trim()}“")
        } finally {
            admin.cmd("ignore remove ${P1}"); sleep(800); resetFriends()
        }
    }

    scenario("friends-14", "Anfrage serverübergreifend") {
        on(LOBBY, p1); on(PAPER, admin)
        resetFriends()
        val ma = admin.mark()
        p1.cmd("friend add ${A}")
        admin.expect("Freundschaftsanfrage von ${P1}", since = ma)
        note("${A} erhält die Anfrage auf Paper")
        resetFriends()
    }

    scenario("friends-15", "[✔] in Anfrage-Nachricht") {
        on(PAPER, admin, p1)
        resetFriends()
        val ma = admin.mark()
        p1.cmd("friend add ${A}")
        val inc = admin.expect("Freundschaftsanfrage von ${P1}", since = ma)
        val m1 = p1.mark(); val m2 = admin.mark()
        admin.clickIn(inc, "✔")
        val a = admin.expect("Du hast die Freundschaftsanfrage von ${P1} angenommen", since = m2)
        val b = p1.expect("${A} hat deine Freundschaftsanfrage angenommen", since = m1)
        note("„${a.plain.trim()}“ / „${b.plain.trim()}“")
        resetFriends()
    }

    scenario("friends-16", "[❌] in Anfrage-Nachricht") {
        on(PAPER, admin, p1)
        resetFriends()
        val ma = admin.mark()
        p1.cmd("friend add ${A}")
        val inc = admin.expect("Freundschaftsanfrage von ${P1}", since = ma)
        val m1 = p1.mark(); val m2 = admin.mark()
        admin.clickIn(inc, "❌")
        val a = admin.expect("abgelehnt", since = m2)
        val b = p1.expect("${A} hat deine Freundschaftsanfrage abgelehnt", since = m1)
        note("„${a.plain.trim()}“ / „${b.plain.trim()}“")
        resetFriends()
    }

    scenario("friends-17", "[Zurückziehen] in Sender-Nachricht") {
        on(PAPER, admin, p1)
        resetFriends()
        val m = p1.run("friend add ${A}")
        val out = p1.expect("Freundschaftsanfrage an ${A} gesendet", since = m)
        val ma = admin.mark(); val m2 = p1.mark()
        val c = p1.clickIn(out, "Zurückziehen")
        p1.expect("zurückgezogen", since = m2)
        admin.expect("zurückgezogen", since = ma)
        note("Klick (${c.value}) zieht die Anfrage zurück")
        resetFriends()
    }

    scenario("friends-18", "/friend accept") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        val sugg = admin.suggestions("/friend accept ")
        val m1 = p1.mark()
        val m = admin.run("friend accept ${P1}")
        admin.expect("angenommen", since = m)
        p1.expect("hat deine Freundschaftsanfrage angenommen", since = m1)
        val rows = net.sql("""SELECT count(*) FROM "surf-friends".friendships WHERE (player_uuid = '${admin.uuid}' AND friend_uuid = '${p1.uuid}') OR (player_uuid = '${p1.uuid}' AND friend_uuid = '${admin.uuid}')""")
        check("${P1}" in sugg) { "Tab-Vervollständigung ohne ${P1}: $sugg" }
        check(rows[0][0].toInt() >= 1) { "Keine friendships-Zeile" }
        note("Tab schlägt $sugg vor; friendships-Zeilen: ${rows[0][0]}")
        resetFriends()
    }

    scenario("friends-19", "/friend accept ohne Anfrage") {
        on(PAPER, admin, p1)
        resetFriends()
        val m = admin.run("friend accept ${P1}")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("keine Freundschaftsanfrage von diesem Spieler")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("friends-20", "/friend accept Groß-/Kleinschreibung") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        val m = admin.run("friend accept ${P1.lowercase()}")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        resetFriends()
        if (!r.plain.contains("angenommen")) bad("„${P1.lowercase()}“ nicht angenommen: „${r.plain.trim()}“")
        note("„${r.plain.trim()}“")
    }

    scenario("friends-21", "/friend deny") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        val m1 = p1.mark()
        val m = admin.run("friend deny ${P1}")
        val a = admin.expect("abgelehnt", since = m)
        p1.expect("${A} hat deine Freundschaftsanfrage abgelehnt", since = m1)
        val cd = net.sql("""SELECT count(*) FROM "surf-friends".friend_request_cooldowns WHERE sender_uuid = '${p1.uuid}' AND receiver_uuid = '${admin.uuid}'""")
        check(cd[0][0] == "1") { "Kein Cooldown-Eintrag" }
        note("„${a.plain.trim()}“; Cooldown für ${P1} angelegt (/friend decline heißt jetzt /friend deny)")
        resetFriends()
    }

    scenario("friends-22", "/friend deny ohne Anfrage") {
        on(PAPER, admin)
        resetFriends()
        val m = admin.run("friend deny ${P1}")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("keine Freundschaftsanfrage von diesem Spieler")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("friends-23", "/friend revoke") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        val ma = admin.mark()
        val m = p1.run("friend revoke ${A}")
        val a = p1.expect("Du hast die Freundschaftsanfrage an ${A} zurückgezogen", since = m)
        val b = admin.expect("Die Freundschaftsanfrage von ${P1} wurde zurückgezogen", since = ma)
        note("„${a.plain.trim()}“ / „${b.plain.trim()}“")
        resetFriends()
    }

    scenario("friends-24", "/friend revoke ohne Anfrage") {
        on(PAPER, p1)
        resetFriends()
        val m = p1.run("friend revoke ${A}")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("keine Freundschaftsanfrage an diesen Spieler")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("friends-25", "/friend requests") {
        on(PAPER, admin, p1)
        resetFriends()
        request(p1, admin)
        val m = admin.run("friend requests")
        val r = admin.expect("${P1}", since = m)
        check(Regex("""\d{2}\.\d{2}\.\d{4}""").containsMatchIn(r.plain)) { "Kein Datum: „${r.plain}“" }
        val click = r.clicks.firstOrNull { it.value.contains("friend accept ${P1}") } ?: bad("Kein Klick „/friend accept ${P1}“: ${r.clicks.map { it.value }}")
        note("„${r.plain.trim().replace("\n", " ⏎ ")}“, Klick ${click.value}")
        resetFriends()
    }

    scenario("friends-26", "/friend requests leer") {
        on(PAPER, admin)
        resetFriends()
        val m = admin.run("friend requests")
        val r = admin.expect("keine offenen Freundschaftsanfragen", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("friends-27", "/friend list") {
        on(PAPER, admin, p1)
        makeFriends(p1, admin)
        quit(p2)
        val m = admin.run("friend list")
        val r = admin.expect("${P1}", since = m)
        check(r.plain.contains("Online auf")) { "Kein „(Online auf …)“: „${r.plain}“" }
        note("„${r.plain.trim().replace("\n", " ⏎ ")}“")
        resetFriends()
    }

    // Changed on purpose: /friend has no short forms any more (docs/command-conventions.md).
    scenario("friends-28", "/fl entfernt") {
        on(PAPER, admin)
        admin.expectCommandUnavailable("fl")
        val m = admin.run("fl")
        val r = admin.firstMessage(m, 3000) ?: bad("Keine Antwort auf /fl")
        check(r.plain.contains("command.unknown.command") || r.plain.contains("Unbekannt", ignoreCase = true)) { "„${r.plain.trim()}“" }
        note("/fl ist unbekannt: „${r.plain.trim().replace("\n", " ⏎ ")}“")
    }

    scenario("friends-29", "/friend list leer") {
        on(PAPER, p1)
        resetFriends()
        val m = p1.run("friend list")
        val r = p1.expect("Du hast keine Freunde", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("friends-30", "/friend info") {
        on(PAPER, admin, p1)
        makeFriends(p1, admin)
        val m = admin.run("friend info ${P1}")
        val r = admin.expect("${P1}", since = m)
        val all = admin.messagesSince(m).joinToString(" ⏎ ") { it.plain.trim() }
        check(Regex("""\d{2}\.\d{2}\.\d{4}""").containsMatchIn(all)) { "Kein Datum: $all" }
        val m2 = admin.run("friend info ${P1.lowercase()}")
        val r2 = admin.firstMessage(m2) ?: bad("Keine Antwort für „${P1.lowercase()}“")
        check(r2.plain.contains("${P1}")) { "Kleinschreibung: „${r2.plain.trim()}“" }
        note("„${all.take(250)}“; „${P1.lowercase()}“ funktioniert")
        resetFriends()
    }

    scenario("friends-31", "/friend info kein Freund") {
        on(PAPER, p1)
        resetFriends()
        val m = p1.run("friend info ${A}")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht mit diesem Spieler befreundet")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("friends-32", "/friend remove") {
        on(PAPER, admin); on(LOBBY, p1)
        makeFriends(p1, admin)
        val m = admin.run("friend remove ${P1}")
        val conf = admin.expect("Möchtest du die Freundschaft mit ${P1} wirklich beenden?", since = m, prefix = Prefix.Warning)
        check(conf.hovers.any { it.second.contains("neue Freundschaftsanfrage") && it.second.contains("Klicke, um zu bestätigen") }) { "Hover: ${conf.hovers.map { it.second }}" }
        val m1 = p1.mark(); val m2 = admin.mark()
        admin.clickIn(conf, "Bestätigen")
        val a = admin.expect("als Freund entfernt", since = m2)
        val b = p1.expect("${A} hat dich als Freund entfernt", since = m1)
        note("Bestätigung „${conf.plain.trim()}“; danach „${a.plain.trim()}“ / ${P1} (Lobby): „${b.plain.trim()}“")
        resetFriends()
    }

    scenario("friends-33", "/friend remove kein Freund") {
        on(PAPER, p1)
        resetFriends()
        val m = p1.run("friend remove ${A}")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht mit diesem Spieler befreundet")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("friends-34", "Join-Hinweis: Freunde online") {
        on(LOBBY, admin)
        makeFriends(p1, admin)
        quit(p1); join(p1)
        val r = p1.expect("Aktuell sind 1 deiner Freunde online", since = 0, timeout = 6000)
        check(r.clicks.any { it.value.contains("friend list") }) { "Klick: ${r.clicks.map { it.value }}" }
        val m = p1.mark(); on(PAPER, p1)
        p1.expectNone("deiner Freunde online", since = m, within = 2500)
        note("Beim Netzwerk-Join (${A} in der Lobby): „${r.plain.trim()}“; beim Serverwechsel kein Hinweis")
        resetFriends()
    }

    scenario("friends-35", "Join-Hinweis: offene Anfragen") {
        on(PAPER, admin)
        resetFriends()
        admin.cmd("friend add ${P1}"); sleep(1500)
        quit(p1); join(p1)
        val r = p1.expect("Du hast noch 1 Freundschaftsanfragen offen", since = 0, timeout = 6000)
        check(r.clicks.any { it.value.contains("friend requests") }) { "Klick: ${r.clicks.map { it.value }}" }
        val m = p1.mark(); on(PAPER, p1)
        p1.expectNone("Freundschaftsanfragen offen", since = m, within = 2500)
        note("„${r.plain.trim()}“ beim Netzwerk-Join, nicht beim Serverwechsel")
        resetFriends()
    }

    scenario("friends-36", "Online-Benachrichtigung") {
        on(PAPER, admin)
        makeFriends(p1, admin)
        quit(p1)
        val m = admin.mark()
        join(p1)
        val r = admin.expect("${P1} ist nun online", since = m, prefix = Prefix.Info)
        sleep(800)
        val sound = admin.since(m).firstOrNull { it.kind == dev.slne.surf.player.tests.bot.Received.Kind.SOUND }
        note("${A} (Paper), ${P1} betritt die Lobby: „${r.plain.trim()}“, Sound ${sound?.extra ?: "keiner"}")
        resetFriends()
        if (sound == null || !sound.extra.contains("experience_orb")) bad("Kein XP-Orb-Sound; ${notes.last()}")
    }

    scenario("friends-37", "Offline-Benachrichtigung") {
        on(PAPER, admin)
        makeFriends(p1, admin)
        val m = admin.mark()
        quit(p1)
        val r = admin.expect("${P1} ist nun offline", since = m)
        sleep(800)
        val sound = admin.since(m).firstOrNull { it.kind == dev.slne.surf.player.tests.bot.Received.Kind.SOUND }
        note("„${r.plain.trim()}“, Sound ${sound?.extra ?: "keiner"}")
        resetFriends()
        if (sound == null || !sound.extra.contains("bass")) bad("Kein Bass-Sound; ${notes.last()}")
    }

    scenario("friends-38", "Benachrichtigung serverübergreifend, nicht bei Serverwechsel") {
        on(PAPER, admin)
        makeFriends(p1, admin)
        quit(p1)
        val m = admin.mark()
        join(p1)
        admin.expect("${P1} ist nun online", since = m)
        val m2 = admin.mark()
        on(PAPER, p1)
        admin.expectNone("${P1} ist nun", since = m2, within = 2500)
        note("Online-Meldung beim Netzwerk-Join (Lobby → ${A} auf Paper), keine beim Wechsel nach Paper")
        resetFriends()
    }

    scenario("friends-39", "Freundes-Sounds") {
        on(PAPER, admin)
        resetFriends()
        val m0 = admin.mark()
        p1.cmd("friend add ${A}"); admin.expect("Freundschaftsanfrage von ${P1}", since = m0)
        admin.cmd("friend accept ${P1}"); sleep(1000)
        val m1 = admin.mark(); quit(p1); admin.expect("ist nun offline", since = m1)
        val m2 = admin.mark(); join(p1); admin.expect("ist nun online", since = m2)
        sleep(800)
        val sounds = admin.since(m0).filter { it.kind == dev.slne.surf.player.tests.bot.Received.Kind.SOUND }.map { it.extra }
        resetFriends()
        val missing = listOf("pling", "bass", "experience_orb").filter { s -> sounds.none { it.contains(s) } }
        note("Sounds: $sounds")
        if (missing.isNotEmpty()) bad("Fehlende Sounds $missing; empfangen $sounds")
    }

    scenario("friends-40", "Sync über Server") {
        on(PAPER, admin, p1)
        makeFriends(p1, admin)
        on(LOBBY, admin)
        val m = admin.run("friend list")
        admin.expect("${P1}", since = m)
        note("Auf Paper erstellte Freundschaft sofort in der Lobby sichtbar")
        resetFriends()
    }

    scenario("friends-41", "Permission-Gate /friend") {
        on(PAPER, p1)
        withPermission(p1, "surf.player.command.friend", false) {
            on(LOBBY, p1); on(PAPER, p1)
            // /fa and /fl were removed on purpose, so nothing friend-related is left.
            p1.expectCommandUnavailable("friend")
            p1.expectCommandUnavailable("fa")
            p1.expectCommandUnavailable("fl")
            note("/friend fehlt bei surf.player.command.friend=false; /fa und /fl gibt es nicht mehr")
        }
    }

    scenario("friends-42", "/friend Hilfe") {
        on(PAPER, p1)
        val help = helpContains(p1, "friend", "add", "list", "info", "remove", "requests", "accept", "deny", "revoke")
        // No short forms any more (removed on purpose), so the help lists none.
        check(!help.contains("/fa") && !help.contains("/fl")) { "Hilfe nennt noch /fa oder /fl: $help" }
        note(help)
    }
    scenario("friends-43", "Alias /f entfernt") { on(PAPER, p1); p1.expectCommandUnavailable("f"); note("/f fehlt im Befehlsbaum") }
    scenario("friends-44", "add: Ziel nimmt keine Anfragen an") {
        on(PAPER, admin, p1); resetFriends()
        setSetting(admin, "friend:requests", false)
        try {
            val m = p1.run("friend add ${A}")
            val r = p1.expect("${A} nimmt keine Freundschaftsanfragen an", since = m, prefix = Prefix.Error)
            val db = net.sql("""SELECT count(*) FROM "surf-friends".friend_requests WHERE sender_uuid = '${p1.uuid}'""")
            check(db[0][0] == "0") { "Anfrage trotzdem gespeichert" }
            note("„${r.plain.trim()}“, keine Anfrage gespeichert")
        } finally { setSetting(admin, "friend:requests", true); resetFriends() }
    }
    scenario("friends-45", "friend:requests aus für Offline-Ziel") {
        on(PAPER, admin, p1); resetFriends()
        setSetting(admin, "friend:requests", false)
        quit(admin)
        try {
            val m = p1.run("friend add ${A}")
            note("„${p1.expect("${A} nimmt keine Freundschaftsanfragen an", since = m).plain.trim()}“ (${A} offline)")
        } finally { on(PAPER, admin); setSetting(admin, "friend:requests", true); resetFriends() }
    }
    scenario("friends-46", "remove: ohne Klick keine Änderung") {
        on(PAPER, admin, p1); makeFriends(p1, admin)
        admin.cmd("friend remove ${P1}"); sleep(1500)
        val m = admin.run("friend list")
        admin.expect("${P1}", since = m)
        note("Ohne Bestätigung bleibt die Freundschaft"); resetFriends()
    }
    scenario("friends-47", "remove: Klick nach Ablauf") {
        on(PAPER, admin, p1); makeFriends(p1, admin)
        val m = admin.run("friend remove ${P1}")
        val conf = admin.expect("wirklich beenden", since = m)
        sleep(65_000)
        admin.clickIn(conf, "Bestätigen"); sleep(2500)
        val rows = net.sql("""SELECT count(*) FROM "surf-friends".friendships WHERE player_uuid IN ('${admin.uuid}','${p1.uuid}')""")
        resetFriends()
        check(rows[0][0] != "0") { "Freundschaft nach Klick >1 min trotzdem beendet" }
        note("Klick nach 65 s wirkungslos")
    }
    scenario("friends-48", "remove: Klick, Freundschaft schon beendet") {
        on(PAPER, admin, p1); makeFriends(p1, admin)
        val m = admin.run("friend remove ${P1}")
        val conf = admin.expect("wirklich beenden", since = m)
        val mp = p1.run("friend remove ${A}")
        p1.clickIn(p1.expect("wirklich beenden", since = mp), "Bestätigen"); sleep(1500)
        val m2 = admin.mark(); admin.clickIn(conf, "Bestätigen")
        note("„${admin.expect("Du bist nicht mehr mit diesem Spieler befreundet", since = m2).plain.trim()}“"); resetFriends()
    }
    scenario("friends-49", "/friend list zeigt vanished Freund als offline") {
        on(PAPER, admin, p1); makeFriends(p1, admin); vanish(admin, true)
        try {
            val m = p1.run("friend list")
            val r = p1.expect("${A}", since = m)
            check(r.plain.lines().first { it.contains("${A}") }.contains("Offline")) { "„${r.plain.trim()}“" }
            note("„${r.plain.trim().replace("\n", " ⏎ ")}“")
        } finally { vanish(admin, false); resetFriends() }
    }
    scenario("friends-50", "/friend list mit Vanish-Bypass") {
        on(PAPER, admin, p1); makeFriends(p1, admin)
        withPermission(p1, "surf.player.vanish.bypass") {
            vanish(admin, true)
            try {
                val m = p1.run("friend list")
                val r = p1.expect("${A}", since = m)
                check(r.plain.lines().first { it.contains("${A}") }.contains("Online")) { "„${r.plain.trim()}“" }
                note("„${r.plain.trim().replace("\n", " ⏎ ")}“")
            } finally { vanish(admin, false) }
        }
        resetFriends()
    }
    scenario("friends-51", "Keine Online-Meldung für vanished Spieler") {
        on(PAPER, admin, p1); makeFriends(p1, admin); vanish(admin, true)
        try {
            val m = p1.mark()
            quit(admin); join(admin)
            p1.expectNone("${A} ist nun", since = m, within = 3000)
            note("Kein „ist nun offline/online“ beim Rejoin des vanished ${A}")
        } finally { vanish(admin, false); resetFriends() }
    }
    scenario("friends-52", "Join-Hinweis zählt vanished Freunde nicht") {
        on(PAPER, admin); makeFriends(p1, admin); vanish(admin, true)
        try {
            quit(p1); join(p1)
            p1.expectNone("deiner Freunde online", since = 0, within = 3000)
            note("Kein „Aktuell sind 1 deiner Freunde online.“")
        } finally { vanish(admin, false); resetFriends() }
    }
    scenario("friends-53", "friend:sounds aus") {
        on(PAPER, admin); resetFriends()
        setSetting(admin, "friend:sounds", false)
        try {
            val m = admin.mark()
            p1.cmd("friend add ${A}"); admin.expect("Freundschaftsanfrage von ${P1}", since = m)
            sleep(1000)
            val sounds = admin.since(m).filter { it.kind == dev.slne.surf.player.tests.bot.Received.Kind.SOUND }.map { it.extra }
            check(sounds.none { it.contains("pling") }) { "Pling trotz friend:sounds aus: $sounds" }
            note("Anfrage ohne Sound")
        } finally { setSetting(admin, "friend:sounds", true); resetFriends() }
    }
    scenario("friends-54", "/friend requests und /friend list Seiten") {
        on(PAPER, admin)
        note("requests 2: „${answer(admin, "friend requests 2").take(120)}“; list 2: „${answer(admin, "friend list 2").take(120)}“")
    }
    scenario("friends-55", "Vorschläge der Argumente") {
        on(PAPER, admin, p1, p2); makeFriends(p1, admin)
        p2.cmd("friend add ${A}"); sleep(1200)
        val acc = admin.suggestions("/friend accept "); val deny = admin.suggestions("/friend deny ")
        val info = admin.suggestions("/friend info "); val rem = admin.suggestions("/friend remove ")
        val rev = p2.suggestions("/friend revoke ")
        note("accept $acc, deny $deny, info $info, remove $rem, revoke (${P2}) $rev")
        resetFriends()
        check("${P2}" in acc && "${P2}" in deny && "${P1}" in info && "${P1}" in rem && "${A}" in rev) { notes.last() }
    }
    scenario("friends-56", "Präfixe der Freundes-Meldungen") {
        on(PAPER, admin, p1); resetFriends()
        val m = p1.run("friend add ${A}"); val ok = p1.expect("gesendet", since = m)
        val m2 = p1.run("friend add ${P1}"); val err = p1.expect("selbst", since = m2)
        val m3 = p2.run("friend list"); val info = p2.firstMessage(m3)
        resetFriends()
        val issues = listOfNotNull(
            if (!hasPrefix(ok, Prefix.Success)) "Erfolg ohne Erfolgs-Präfix" else null,
            if (!hasPrefix(err, Prefix.Error)) "Fehler ohne Fehler-Präfix" else null,
            if (info != null && !hasPrefix(info, Prefix.Info)) "Liste „${info.plain.trim()}“ ohne Info-Präfix" else null,
        )
        if (issues.isNotEmpty()) bad(issues.joinToString("; "))
        note("Erfolg grün, Fehler rot, leere Liste Info")
    }
}
