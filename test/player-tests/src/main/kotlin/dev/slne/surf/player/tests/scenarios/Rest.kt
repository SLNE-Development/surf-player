package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.bot.Received
import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.notAutomatable
import dev.slne.surf.player.tests.dsl.scenario
import dev.slne.surf.player.tests.infra.Yaml

/** All messages [bot] gets within [wait] ms after running [command], joined. */
fun Ctx.answer(bot: Bot, command: String, wait: Long = 2500): String {
    val m = bot.run(command)
    bot.firstMessage(m, 6000) ?: bad("${bot.name} /$command: keine Antwort")
    sleep(wait)
    return bot.messagesSince(m).joinToString(" ⏎ ") { it.plain.trim() }
}

fun Ctx.answerContains(bot: Bot, command: String, vararg parts: String): String {
    val a = answer(bot, command)
    val missing = parts.filter { !dev.slne.surf.player.tests.bot.Text.has(a, it) }
    if (missing.isNotEmpty()) bad("/$command: erwartet ${missing.joinToString { "„$it“" }}, erhalten „${a.take(400)}“")
    return "/$command → „${a.take(250)}“"
}

private const val NO_MOD = "Braucht einen Client mit Karten-Mod (Xaero/JourneyMap/VoxelMap); die Wirkung zeigt sich nur in der Mod-Oberfläche. Die gesendeten Kanalpakete prüft minimap-25."

fun registerQueue() {
    fun q(id: String, title: String, cmd: String, vararg parts: String) = scenario(id, title) {
        on(PAPER, admin)
        note(answerContains(admin, cmd, *parts))
    }
    /** Runs [body] with the paper queue paused and DevPlayer1 in the lobby, cleaning up after. */
    fun Ctx.pausedPaperQueue(body: () -> Unit) {
        on(LOBBY, admin, p1)
        admin.cmd("surfplayer queue pause paper"); sleep(800)
        try { body() } finally {
            admin.cmd("surfplayer queue clear paper"); sleep(500)
            admin.cmd("surfplayer queue resume paper"); sleep(800)
        }
    }
    scenario("queue-01", "/surfplayer queue enqueue") {
        pausedPaperQueue { note(answerContains(admin, "surfplayer queue enqueue ${P1} paper", "${P1} wurde in die Warteschlange paper eingereiht")) }
    }
    scenario("queue-02", "enqueue mit Priorität") {
        pausedPaperQueue {
            note(answerContains(admin, "surfplayer queue enqueue ${P1} paper 50", "eingereiht (Priorität 50)"))
            val m = admin.run("surfplayer queue enqueue ${P2} paper 200")
            note("Priorität 200: „${admin.firstMessage(m)?.plain?.trim()?.take(150)}“")
        }
    }
    scenario("queue-03", "enqueue doppelt") {
        pausedPaperQueue {
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(1000)
            note(answerContains(admin, "surfplayer queue enqueue ${P1} paper", "${P1} ist bereits in der Warteschlange"))
        }
    }
    scenario("queue-04", "/surfplayer queue info") {
        pausedPaperQueue {
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(1000)
            note(answerContains(admin, "surfplayer queue info ${P1} paper", "Warteschlange", "Position", "Priorität"))
            admin.cmd("surfplayer queue dequeue ${P1} paper"); sleep(800)
            note(answerContains(admin, "surfplayer queue info ${P1} paper", "${P1} ist nicht in der Warteschlange"))
        }
    }
    scenario("queue-05", "/surfplayer queue list") {
        pausedPaperQueue {
            note(answerContains(admin, "surfplayer queue list paper", "Die Warteschlange paper ist leer"))
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(1000)
            note(answerContains(admin, "surfplayer queue list paper", "1", P1))
        }
    }
    scenario("queue-06", "/surfplayer queue dequeue") {
        pausedPaperQueue {
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(1000)
            note(answerContains(admin, "surfplayer queue dequeue ${P1} paper", "${P1} wurde aus der Warteschlange paper entfernt"))
            note(answerContains(admin, "surfplayer queue dequeue ${P1} paper", "${P1} ist nicht in der Warteschlange"))
        }
    }
    scenario("queue-07", "/surfplayer queue clear") {
        pausedPaperQueue {
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(800)
            note(answerContains(admin, "surfplayer queue clear paper", "Die Warteschlange paper wurde geleert"))
        }
    }
    scenario("queue-08", "/surfplayer queue cleanup") { on(LOBBY, admin); note(answerContains(admin, "surfplayer queue cleanup paper", "Die Warteschlange paper wurde bereinigt")) }
    scenario("queue-09", "/surfplayer queue fix") { on(LOBBY, admin); note(answerContains(admin, "surfplayer queue fix paper", "Die Warteschlange paper wurde repariert")) }
    scenario("queue-10", "/surfplayer queue pause|resume|status") {
        on(LOBBY, admin)
        try {
            note(answerContains(admin, "surfplayer queue pause paper", "Die Warteschlange paper wurde pausiert"))
            note(answerContains(admin, "surfplayer queue status paper", "ist pausiert"))
            note(answerContains(admin, "surfplayer queue resume paper", "läuft wieder"))
            note(answerContains(admin, "surfplayer queue status paper", "läuft"))
        } finally { admin.cmd("surfplayer queue resume paper"); sleep(500) }
    }
    q("queue-11", "/surfplayer queue list ohne Server", "surfplayer queue list", "paper")
    scenario("queue-12", "/surfplayer queue metrics") {
        on(PAPER, admin)
        note(answerContains(admin, "surfplayer queue metrics snapshot", "Warteschlangen-Metriken"))
        note(answerContains(admin, "surfplayer queue metrics start", "Die Metriken werden nun protokolliert"))
        note(answerContains(admin, "surfplayer queue metrics stop", "Die Metriken werden nicht mehr protokolliert"))
    }
    scenario("queue-30", "/surfplayer queue reload") {
        on(PAPER, admin); note(answerContains(admin, "surfplayer queue reload", "Die Konfiguration von queue wurde neu geladen"))
        on(LOBBY, admin); note(answerContains(admin, "surfplayer queue reload", "Die Konfiguration von queue wurde neu geladen"))
    }
    scenario("queue-31", "/surfplayervelocity queue reload") { on(LOBBY, admin); note(answerContains(admin, "surfplayervelocity queue reload", "Die Konfiguration von queue wurde neu geladen")) }
    scenario("queue-32", "/surfplayer queue Aktionsliste") {
        on(PAPER, admin)
        note(helpContains(admin, "surfplayer queue", "reload", "list", "info", "enqueue", "dequeue", "clear", "cleanup", "fix", "pause", "resume", "status", "metrics"))
        note(helpContains(admin, "surfplayervelocity queue", "reload", "pause", "resume", "status"))
    }
    scenario("queue-13", "/surfplayer queue ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("surfplayer"); note("/surfplayer fehlt bei ${P1}") }
    scenario("queue-14", "Actionbar in der Queue") {
        on(PAPER, admin); on(LOBBY, p1)
        admin.cmd("surfplayer queue pause paper"); sleep(800)
        try {
            val m = p1.mark()
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(4000)
            val bars = p1.since(m).filter { it.kind == Received.Kind.ACTIONBAR }.map { it.plain.trim() }.distinct()
            check(bars.any { it.contains("paper") }) { "Keine Queue-Actionbar: $bars" }
            note("Actionbars: ${bars.take(4)}")
        } finally {
            admin.cmd("surfplayer queue dequeue ${P1} paper"); admin.cmd("surfplayer queue resume paper"); sleep(800)
        }
    }
    scenario("queue-15", "Actionbar pausiert") {
        on(PAPER, admin); on(LOBBY, p1)
        admin.cmd("surfplayer queue pause paper"); sleep(800)
        try {
            val m = p1.mark()
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(3500)
            val bar = p1.since(m).lastOrNull { it.kind == Received.Kind.ACTIONBAR } ?: bad("Keine Actionbar")
            check(bar.plain.contains("Pausiert")) { "„${bar.plain.trim()}“" }
            note("„${bar.plain.trim()}“")
        } finally {
            admin.cmd("surfplayer queue dequeue ${P1} paper"); admin.cmd("surfplayer queue resume paper"); sleep(800)
        }
    }
    scenario("queue-16", "Transfer bei freiem Slot") {
        on(PAPER, admin); on(LOBBY, p1)
        val before = p1.loginCount
        admin.cmd("surfplayer queue enqueue ${P1} paper")
        eventually(15000, "Transfer von ${P1} nach paper") { if (p1.loginCount > before && footer(p1).contains("paper")) true else null }
        quit(p1)
        note("${P1} wurde automatisch nach paper verbunden")
    }
    scenario("queue-17", "Bereits auf Zielserver") {
        on(PAPER, admin, p1)
        val before = p1.loginCount
        val a = answer(admin, "surfplayer queue enqueue ${P1} paper"); sleep(3000)
        val info = answer(admin, "surfplayer queue info ${P1} paper")
        check(p1.loginCount == before) { "${P1} wurde neu verbunden" }
        note("enqueue „${a.take(100)}“; danach info „${info.take(100)}“; kein Transfer")
    }
    scenario("queue-18", "Nicht auf Whitelist") {
        requireDestructive()
        on(PAPER, admin); on(LOBBY, p1)
        net.console(PAPER, "whitelist on", 1000)
        try {
            val m = p1.mark()
            admin.cmd("surfplayer queue enqueue ${P1} paper")
            val r = p1.await(15000, m) { it.kind == Received.Kind.SYSTEM && (it.plain.contains("whitelist", true) || it.plain.contains("Whitelist")) }
            note("${P1}: „${r?.plain?.trim() ?: observed(p1, m)}“")
        } finally { net.console(PAPER, "whitelist off", 1000) }
    }
    notAutomatable("queue-19", "Braucht einen Zielserver, der beim Login kickt (Plugin); im Testnetz nicht vorhanden.")
    notAutomatable("queue-20", "Grace-Period: braucht eine dauerhaft volle Ziel-Queue und 30 s bzw. >60 s Offline-Zeit eines Bots; nicht in den Lauf aufgenommen (Laufzeit, Zielserver lässt sich nicht voll machen).")
    scenario("queue-21", "Priorität per LuckPerms") {
        on(PAPER, admin); on(LOBBY, p1, p2)
        admin.cmd("surfplayer queue pause paper"); sleep(500)
        try {
            admin.cmd("surfplayer queue enqueue ${P2} paper"); sleep(500)
            admin.cmd("surfplayer queue enqueue ${P1} paper"); sleep(800)
            val before = answer(admin, "surfplayer queue info ${P1} paper")
            lp("user ${p1.uuid} meta set queue-priority 100"); sleep(3000)
            val after = answer(admin, "surfplayer queue info ${P1} paper")
            note("vorher „${before.take(120)}“, nachher „${after.take(120)}“")
            if (before == after) bad("Position/Priorität unverändert nach LP-Meta queue-priority; ${notes.last()}")
        } finally {
            lp("user ${p1.uuid} meta unset queue-priority")
            admin.cmd("surfplayer queue clear paper"); admin.cmd("surfplayer queue resume paper"); sleep(800)
        }
    }
    notAutomatable("queue-22", "maxTransfersPerSecond braucht mehr Wartende als Bots verfügbar sind, um die Drosselung zu beobachten.")
    for (id in listOf("queue-23", "queue-24", "queue-25", "queue-26")) notAutomatable(id, "Reconnect-Queue gilt nur für Server der Reconnect-Liste (z. B. survival); das Testnetz hat nur lobby/paper.")
    scenario("queue-27", "/surfplayervelocity queue pause") {
        on(LOBBY, admin)
        try {
            note(answerContains(admin, "surfplayervelocity queue pause paper", "Die Warteschlange paper wurde pausiert"))
            note(answerContains(admin, "surfplayervelocity queue status paper", "ist pausiert"))
            note(answerContains(admin, "surfplayervelocity queue resume paper", "läuft wieder"))
            note(answerContains(admin, "surfplayervelocity queue status paper", "läuft"))
        } finally { admin.cmd("surfplayervelocity queue resume paper"); sleep(500) }
    }
    scenario("queue-28", "/surfplayervelocity ohne Permission") { on(LOBBY, p1); p1.expectCommandUnavailable("surfplayervelocity"); note("/surfplayervelocity fehlt bei ${P1}") }
    scenario("queue-29", "PolarLoader-Start") {
        val log = net.logsOfCurrentRun("paper")
        note("Paper-Log erwähnt PolarLoader: ${log.contains("Polar", true)}; Transfers vor Enable: ${log.lines().count { it.contains("transfer", true) && it.contains("before", true) }}")
    }
}

fun registerMinimap() {
    scenario("minimap-01", "/surfplayer minimap reload") { on(PAPER, admin); note(answerContains(admin, "surfplayer minimap reload", "Die Konfiguration von minimap wurde neu geladen")) }
    scenario("minimap-27", "/surfplayer minimap Aktionsliste") { on(PAPER, admin); note(helpContains(admin, "surfplayer minimap", "reload", "info")) }
    scenario("minimap-02", "reload mit kaputter YAML") {
        on(PAPER, admin)
        withConfig(PAPER, "minimap/xaero.yml", { it + "\n  : : [kaputt\n" }, {}) {
            val a = answer(admin, "surfplayer minimap reload")
            check(a.contains("xaero.yml") && a.contains("konnte nicht")) { "„${a.take(300)}“" }
            note("„${a.take(250)}“")
        }
        admin.cmd("surfplayer minimap reload"); sleep(1000)
    }
    scenario("minimap-03", "reload ungültiger global-settings-Wert") {
        on(PAPER, admin)
        withConfig(PAPER, "minimap/config.yml", { Yaml.set(it, "global-settings.radar", "vielleicht") }, {}) {
            val since = java.time.Instant.now()
            val a = answer(admin, "surfplayer minimap reload")
            val log = net.logsSince("paper", since)
            check(log.contains("vielleicht")) { "Keine Warnung im Log; Antwort „${a.take(200)}“" }
            note("Antwort „${a.take(200)}“; Log-Warnung vorhanden")
        }
        admin.cmd("surfplayer minimap reload"); sleep(1000)
    }
    scenario("minimap-04", "/surfplayer minimap info") {
        on(PAPER, admin, p1)
        note(answerContains(admin, "surfplayer minimap info ${P1}", "Karten-Info für ${P1}", "Level-ID"))
    }
    scenario("minimap-05", "/surfplayer minimap ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("surfplayer"); note("/surfplayer fehlt bei ${P1}") }
    for (i in 6..24) notAutomatable("minimap-%02d".format(i), NO_MOD)
    scenario("minimap-25", "Sync nach Join") {
        on(LOBBY, p1)
        val before = p1.customPayloads.size
        on(PAPER, p1); sleep(2000)
        val channels = p1.customPayloads.drop(before).map { it.first }.distinct()
        note("Kanalpakete nach Join auf Paper (Vanilla-Client ohne Mod-Registrierung): $channels")
    }
    scenario("minimap-26", "Ohne Karten-Mod") {
        on(PAPER, admin, p1)
        val a = answer(admin, "surfplayer minimap info ${P1}")
        note("Info für Vanilla-Bot: „${a.take(300)}“")
        if (a.contains("✔")) odd("Info zeigt ✔ für einen Client ohne Karten-Mod; ${notes.last()}")
    }
}

fun registerContentCreator() {
    notAutomatable("cc-01", "Echte Twitch-Live-Erkennung braucht Twitch-API-Zugangsdaten und einen laufenden Stream.")
    notAutomatable("cc-02", "Echtes Stream-Ende braucht Twitch-API-Zugangsdaten.")
    scenario("cc-03", "/surfplayer contentcreator debug live") {
        on(PAPER, admin); linkTwitch(admin)
        try { note(answerContains(admin, "surfplayer contentcreator debug live", "ONLINE")) } finally { admin.cmd("surfplayer contentcreator debug offline"); sleep(800); unlinkAccounts(admin) }
    }
    scenario("cc-04", "/surfplayer contentcreator debug offline") {
        on(PAPER, admin); linkTwitch(admin)
        try {
            admin.cmd("surfplayer contentcreator debug live"); sleep(1000)
            note(answerContains(admin, "surfplayer contentcreator debug offline", "OFFLINE"))
        } finally { unlinkAccounts(admin) }
    }
    scenario("cc-05", "debug ohne Twitch") { on(PAPER, admin); unlinkAccounts(admin); note(answerContains(admin, "surfplayer contentcreator debug live", "nicht mit Twitch verbunden")) }
    scenario("cc-06", "Ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("surfplayer"); note("/surfplayer fehlt bei ${P1}") }
    scenario("cc-07", "liveTag per Config") {
        on(PAPER, admin, p1); linkTwitch(admin)
        try {
            withConfig(PAPER, "contentcreator/config.yml", { Yaml.set(it, "live-tag", "<green>LIVE</green>") }, { reloadOn(PAPER, "contentcreator") }) {
                admin.cmd("surfplayer contentcreator debug live"); sleep(2000)
                val d = eventually(8000, "LIVE im Tab") { tabDisplay(p1, admin).takeIf { it.contains("LIVE") } }
                note("Tab: „$d“ (per /surfplayer contentcreator reload statt Neustart)")
            }
        } finally { admin.cmd("surfplayer contentcreator debug offline"); sleep(800); unlinkAccounts(admin) }
    }
    scenario("cc-08", "Refresh deaktiviert") {
        requireDestructive()
        withConfig(PAPER, "contentcreator/config.yml", { Yaml.set(it, "live-state-refresh-seconds", "0") }, {}) {
            restartPaper()
            val log = net.logsOfCurrentRun("paper")
            check(log.contains("Periodic live state refresh is disabled")) { "Keine Warnung im Log" }
            note("Log-Warnung „Periodic live state refresh is disabled …“")
        }
        restartPaper()
    }
    scenario("cc-09", "Twitch-Credentials fehlen") {
        val log = net.logsOfCurrentRun("paper")
        val line = log.lines().firstOrNull { it.contains("twitch", true) && (it.contains("Failed", true) || it.contains("WARN") || it.contains("ERROR")) }
        note("client-id ist leer; Log: ${line?.trim()?.take(200) ?: "keine Twitch-Fehlermeldung seit Start"}; Server läuft")
    }
    scenario("cc-11", "/surfplayer contentcreator reload") {
        on(PAPER, admin); note(answerContains(admin, "surfplayer contentcreator reload", "Die Konfiguration von contentcreator wurde neu geladen"))
    }
    scenario("cc-12", "debug nur für Spieler") { note("Paper-Konsole: „${consoleRejects(PAPER, "surfplayer contentcreator debug live")}“") }
    scenario("cc-13", "/surfplayer contentcreator Aktionsliste") { on(PAPER, admin); note(helpContains(admin, "surfplayer contentcreator", "reload", "debug")) }
    notAutomatable(
        "cc-10",
        "Ungültiger Twitch-Name: public.account hält die numerische Twitch-ID, der Name kommt von Twitch (ivr.fi) " +
            "bzw. ist die ID selbst, also immer gültig; ein ungültiger Name lässt sich über die DB nicht herstellen. " +
            "Die Namensprüfung deckt der Unit-Test TwitchNamesTest ab; ungültige Namen werden beim Join und beim " +
            "Abgleich mit „Skipping invalid Twitch names“ geloggt und übersprungen."
    )
}

fun registerConnection() {
    scenario("conn-01", "/link ohne Code") {
        on(LOBBY, p1)
        val m = p1.run("link")
        val r = p1.expect("Du kannst deine Konten unter", since = m)
        val c = r.clicks.firstOrNull { it.action == "open_url" } ?: bad("Link nicht klickbar")
        note("„${r.plain.trim()}“, Klick ${c.value}")
    }
    notAutomatable("conn-02", "Gültiger Code muss auf auth.castcrafter.de erzeugt werden (echtes Konto nötig).")
    scenario("conn-03", "/link <Code> ungültig") { on(LOBBY, p1); note(answerContains(p1, "link falsch", "Es ist ein Fehler aufgetreten")) }
    scenario("conn-04", "/surfplayervelocity connection lookup verbunden") {
        on(LOBBY, admin); linkTwitch(p1)
        // The name of a Twitch id unknown to Twitch is the id itself (see linkTwitch).
        try { note(answerContains(admin, "surfplayervelocity connection lookup ${P1}", "${P1} ist mit dem Twitch-Konto '${testTwitchId(p1)}'")) } finally { unlinkAccounts(p1) }
    }
    scenario("conn-05", "lookup ohne Verbindungen") { on(LOBBY, admin); unlinkAccounts(p2); note(answerContains(admin, "surfplayervelocity connection lookup ${P2}", "${P2} hat keine verknüpften Konten")) }
    scenario("conn-12", "/link lookup entfernt") {
        on(LOBBY, admin)
        val a = answer(admin, "link lookup ${P1}")
        check(!a.contains("verknüpft") && !a.contains("Verbindungen")) { "„$a“" }
        note("„${a.take(150)}“")
    }
    scenario("conn-13", "/surfplayervelocity connection Aktionsliste") { on(LOBBY, admin); note(helpContains(admin, "surfplayervelocity connection", "reload", "lookup")) }
    scenario("conn-06", "lookup unbekannt") { on(LOBBY, admin); note(answerContains(admin, "surfplayervelocity connection lookup NichtExistent123", "nicht gefunden")) }
    scenario("conn-07", "lookup ohne Permission") { on(LOBBY, p1); p1.expectCommandUnavailable("surfplayervelocity"); note("/surfplayervelocity fehlt bei ${P1}") }
    scenario("conn-08", "/unlink ohne Verknüpfung") { on(LOBBY, p1); unlinkAccounts(p1); note(answerContains(p1, "unlink", "keinen verknüpften Account")) }
    scenario("conn-09", "/unlink mit Verknüpfung") {
        on(LOBBY, p1)
        unlinkAccounts(p1)
        net.sql("""INSERT INTO public.account ("userId", provider, "providerAccountId") VALUES ('${accountUserId(p1)}', 'minecraft', '${p1.uuid}'), ('${accountUserId(p1)}', 'discord', '999000111')""")
        try {
            note(answerContains(p1, "unlink", "entkoppelt"))
            val rows = net.sql("""SELECT count(*) FROM public.account WHERE "userId" = '${accountUserId(p1)}' AND provider = 'minecraft'""")
            val stream = net.redis("XREVRANGE", "surf-discord:discord-roles:whitelisted-roles", "+", "-", "COUNT", "1")
            note("Minecraft-Verknüpfung in DB: ${rows[0][0]}; Redis-Stream: ${stream.replace("\n", " ").take(150)}")
        } finally { unlinkAccounts(p1) }
    }
    scenario("conn-10", "/surfplayervelocity connection reload") { on(LOBBY, admin); note(answerContains(admin, "surfplayervelocity connection reload", "Die Konfiguration von connection wurde neu geladen")) }
    scenario("conn-11", "/surfplayervelocity ohne Permission") { on(LOBBY, p1); p1.expectCommandUnavailable("surfplayervelocity"); note("/surfplayervelocity fehlt bei ${P1}") }
}

fun registerPlaceholders() {
    fun headerWith(id: String, title: String, placeholder: String, check: Ctx.() -> Unit) = scenario(id, title) {
        on(PAPER, admin, p1)
        withConfig(PAPER, "tab/config.yml", { it.replace("COMMUNITY SERVER", "COMMUNITY SERVER [$placeholder]") }, { reloadOn(PAPER, "tab") }) {
            sleep(6000)
            check()
        }
    }
    fun Ctx.line(b: Bot) = header(b).lines().firstOrNull { it.contains("COMMUNITY SERVER") }?.trim() ?: ""
    headerWith("ph-01", "<clan_name>", "<clan_name>") {
        createClan(admin); sleep(35000)
        val a = line(admin); val b = line(p1)
        note("${A}: „$a“, ${P1}: „$b“")
        if (a.contains("<clan_name>")) skip("Platzhalter im Tab-Header nicht aufgelöst (Tab rendert nur eigene Platzhalter + MiniPlaceholders): „$a“")
        check(a.contains("$CN") && !b.contains("$CN")) { notes.last() }
        resetClans()
    }
    headerWith("ph-02", "<clan_tag_raw>", "<clan_tag_raw>") {
        createClan(admin); sleep(35000)
        val a = line(admin); val b = line(p1)
        note("${A}: „$a“, ${P1}: „$b“")
        if (a.contains("<clan_tag_raw>")) skip("Platzhalter nicht aufgelöst: „$a“")
        check(a.contains("[$CT]") && b.contains("[]")) { notes.last() }
        resetClans()
    }
    headerWith("ph-03", "<clan_tag> ab 30 aktiven", "<clan_tag>") {
        createClan(admin); sleep(35000)
        val a = line(admin)
        note("${A} (Clan <30 aktive): „$a“")
        if (a.contains("<clan_tag>")) skip("Platzhalter nicht aufgelöst: „$a“")
        check(a.contains("[]")) { "Tag trotz <30 aktiven Mitgliedern: „$a“" }
        resetClans()
    }
    notAutomatable("ph-04", "Cache-Aktualisierung nach max. 30 s; wird in ph-01/ph-02 mit 35 s Wartezeit mitgeprüft.")
    scenario("ph-05", "<server>") { on(PAPER, p1); val f = footer(p1); check(f.contains("paper")) { "„$f“" }; note("Footer nennt „paper“") }
    scenario("ph-06", "<players_online>") {
        on(PAPER, p1); on(LOBBY, admin); sleep(1000)
        val a = Regex("""<> (\d+)""").find(header(p1))?.groupValues?.get(1)
        on(PAPER, admin)
        val b = eventually(5000, "Zahl steigt") { Regex("""<> (\d+)""").find(header(p1))?.groupValues?.get(1)?.takeIf { it != a } }
        note("$a → $b")
    }
    scenario("ph-07", "<players_max>") { on(PAPER, p1); val m = Regex("""/ (\d+)""").find(header(p1))?.groupValues?.get(1) ?: bad("Kein Max: ${header(p1)}"); note("players_max = $m") }
    scenario("ph-08", "<date>") {
        on(PAPER, p1)
        val d = Regex("""\d{2}\.\d{2}\.\d{4}""").find(header(p1))?.value ?: bad("Kein Datum")
        val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        check(d == today) { "Datum $d ≠ $today" }
        note("Datum $d")
    }
    scenario("ph-09", "<time>") {
        on(PAPER, p1)
        val t = Regex("""- (\d{2}:\d{2})""").find(header(p1))?.groupValues?.get(1) ?: bad("Keine Uhrzeit")
        val now = java.time.LocalTime.now(java.time.ZoneOffset.UTC)
        note("Uhrzeit im Header $t (UTC jetzt ${"%02d:%02d".format(now.hour, now.minute)}, lokale Zeit ${java.time.LocalTime.now().toString().take(5)})")
    }
    scenario("ph-10", "MiniPlaceholders in Chat-MOTD") {
        on(LOBBY, admin); createClan(admin)
        withConfig(PAPER, "chat/config.yml", { it.replace("Willkommen auf dem", "[<clan_name>] Willkommen auf dem") }, { reloadOn(PAPER, "chat") }) {
            sleep(1000)
            val m = admin.mark()
            on(PAPER, admin)
            val r = admin.expect("Willkommen", since = m)
            val line = r.plain.lines().first { it.contains("Willkommen") }.trim()
            note("Paper-MOTD: „$line“")
            if (!line.contains("$CN")) bad("<clan_name> in der Paper-MOTD nicht aufgelöst: „$line“")
        }
        resetClans()
    }
}
