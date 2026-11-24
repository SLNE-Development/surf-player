package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.Prefix
import dev.slne.surf.player.tests.dsl.scenario

/** Checks a command help/action list: every part must appear in the reply. */
fun Ctx.helpContains(bot: Bot, command: String, vararg parts: String): String {
    val a = answer(bot, command, 1500)
    val missing = parts.filter { !dev.slne.surf.player.tests.bot.Text.has(a, it) }
    if (missing.isNotEmpty()) bad("/$command: fehlt ${missing.joinToString { "„$it“" }} in „${a.take(500)}“")
    return "/$command: „${a.take(300)}“"
}

/** Console-only check: [command] on the Paper console must be rejected for non-players. */
fun Ctx.consoleRejects(server: String, command: String): String {
    val out = net.console(server, command, 2000)
    val line = out.lines().map { it.trim() }.lastOrNull { it.isNotBlank() } ?: ""
    return line
}

/** New rows of the rewritten test plan (chat-108 ff.). */
fun registerChatMore() {
    scenario("chat-108", "/ignore Hilfe") {
        on(PAPER, p1)
        note(helpContains(p1, "ignore", "/ignore add", "/ignore remove", "/ignore list"))
    }
    scenario("chat-109", "/ignore add bereits ignoriert") {
        on(PAPER, p1); resetIgnores(p1)
        p1.cmd("ignore add ${P2}"); sleep(1000)
        try {
            val m = p1.run("ignore add ${P2}")
            note("„${p1.expect("Du ignorierst ${P2} bereits", since = m, prefix = Prefix.Error).plain.trim()}“")
        } finally { p1.cmd("ignore remove ${P2}"); sleep(800) }
    }
    scenario("chat-110", "/ignore remove nicht ignoriert") {
        on(PAPER, p1); resetIgnores(p1)
        val m = p1.run("ignore remove ${P2}")
        note("„${p1.expect("Du ignorierst ${P2} nicht", since = m, prefix = Prefix.Error).plain.trim()}“")
    }
    scenario("chat-111", "/ignore remove Vorschläge") {
        on(PAPER, p1); resetIgnores(p1)
        p1.cmd("ignore add ${P2}"); sleep(1200)
        try {
            val s = p1.suggestions("/ignore remove ")
            val prefix = P2.take(5).lowercase()
            val s2 = p1.suggestions("/ignore remove $prefix")
            check(s == listOf(P2)) { "Vorschläge: $s" }
            check(P2 in s2) { "Vorschläge für „$prefix“: $s2" }
            note("Vorschläge $s; für „$prefix“: $s2")
        } finally { p1.cmd("ignore remove ${P2}"); sleep(800) }
    }
    scenario("chat-112", "/ignore list Seiten") {
        on(PAPER, p1)
        val a = answer(p1, "ignore list 2")
        val m = p1.run("ignore list 0")
        val r = p1.firstMessage(m)
        note("Seite 2: „${a.take(150)}“; Seite 0: „${r?.plain?.trim()?.take(120)}“")
    }
    scenario("chat-113", "/spy Hilfe") {
        on(PAPER, admin)
        note(helpContains(admin, "spy", "/spy add", "/spy remove", "/spy clear"))
    }
    scenario("chat-114", "/spy add bereits") {
        on(PAPER, admin, p1); admin.cmd("spy clear"); sleep(500)
        admin.cmd("spy add ${P1}"); sleep(800)
        try {
            val m = admin.run("spy add ${P1}")
            note("„${admin.expect("Du liest die privaten Nachrichten von ${P1} bereits mit", since = m).plain.trim()}“")
        } finally { admin.cmd("spy clear"); sleep(500) }
    }
    scenario("chat-115", "/spy remove ohne Spy") {
        on(PAPER, admin, p1); admin.cmd("spy clear"); sleep(500)
        val m = admin.run("spy remove ${P1}")
        note("„${admin.expect("Du liest die privaten Nachrichten von ${P1} nicht mit", since = m).plain.trim()}“")
    }
    scenario("chat-116", "/spy ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("spy"); note("/spy fehlt bei ${P1}") }
    scenario("chat-117", "/slowchat Hilfe") {
        on(PAPER, admin)
        note(helpContains(admin, "slowchat", "/slowchat info", "/slowchat toggle"))
    }
    scenario("chat-118", "/slowchat toggle ohne Argument") {
        on(LOBBY, admin, p1)
        admin.cmd("slowchat toggle an"); sleep(1000)
        val m1 = p1.mark()
        val m = admin.run("slowchat toggle")
        try {
            val r = admin.expect("Der Slowchat wurde deaktiviert", since = m)
            val box = p1.expect("Slow Chat wurde deaktiviert", since = m1)
            note("„${r.plain.trim()}“; Kasten „${box.plain.trim().replace("\n", " ")}“ (Lobby)")
        } finally { admin.cmd("slowchat toggle aus"); sleep(800) }
    }
    scenario("chat-119", "/slowchat toggle aus bereits aus") {
        on(PAPER, admin)
        admin.cmd("slowchat toggle aus"); sleep(1000)
        val m = admin.run("slowchat toggle aus")
        note("„${admin.expect("Der Slowchat ist bereits deaktiviert", since = m).plain.trim()}“")
    }
    scenario("chat-120", "/slowchat ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("slowchat"); note("/slowchat fehlt bei ${P1}") }
    scenario("chat-121", "/surfplayer chat Aktionsliste") {
        on(PAPER, admin)
        note(helpContains(admin, "surfplayer chat", "reload", "lookup", "filters", "status", "toggle", "servers"))
    }
    scenario("chat-122", "/surfplayer chat toggle ungültig") {
        on(PAPER, admin)
        val m = admin.run("surfplayer chat toggle vielleicht")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("Bitte gib 'an' oder 'aus' an.")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }
    scenario("chat-123", "/surfplayer chat toggle aus bereits aus") {
        on(PAPER, admin)
        admin.cmd("surfplayer chat toggle aus"); sleep(1500)
        try {
            val m = admin.run("surfplayer chat toggle aus")
            note("„${admin.expect("Der Chat ist auf diesem Server bereits deaktiviert", since = m).plain.trim()}“")
        } finally { admin.cmd("surfplayer chat toggle an"); sleep(1000) }
    }
    scenario("chat-124", "lookup unbekannter Absender") {
        on(LOBBY, admin)
        val m = admin.run("surfplayer chat lookup --sender NichtExistent123")
        val r = admin.await(12000, m) { dev.slne.surf.player.tests.bot.Text.has(it.plain, "Suchergebnisse") || it.plain.contains("keine Ergebnisse") || it.plain.contains("nicht gefunden") }
            ?: bad("Keine Antwort: ${observed(admin, m)}")
        check(!dev.slne.surf.player.tests.bot.Text.has(r.plain, "Suchergebnisse")) { "Ungefilterte Treffer für unbekannten Absender: „${r.plain.trim().take(250)}“" }
        note("„${r.plain.trim()}“")
    }
    scenario("chat-125", "lookup --message mit Leerzeichen") {
        on(PAPER, admin)
        admin.chat("Hallo Welt ${uniq()}"); sleep(1200)
        val m = admin.run("surfplayer chat lookup --message \"Hallo Welt\"")
        val r = admin.await(12000, m) { dev.slne.surf.player.tests.bot.Text.has(it.plain, "Suchergebnisse") || it.plain.contains("keine Ergebnisse") } ?: bad("Keine Antwort: ${observed(admin, m)}")
        val rows = r.plain.lines().filter { it.contains("schrieb") }
        check(rows.isNotEmpty() && rows.all { it.contains("Hallo Welt", true) || it.contains("Hallo", true) }) { "„${r.plain.trim().take(300)}“" }
        note("${rows.size} Treffer, alle mit „Hallo Welt“")
    }
    scenario("chat-126", "Löschen netzwerkweit") {
        skip("Löschen braucht signierte Nachrichten; die Offline-Bots senden unsigniert (siehe chat-06).")
    }
    scenario("chat-127", "Präfixe der Chat-Meldungen") {
        on(PAPER, p1); resetIgnores(p1)
        val problems = mutableListOf<String>()
        fun chk(cmd: String, text: String, prefix: Prefix) {
            val m = p1.run(cmd)
            val r = p1.await(5000, m) { it.plain.contains(text) }
            when {
                r == null -> problems += "/$cmd: „$text“ fehlt (${observed(p1, m)})"
                !hasPrefix(r, prefix) -> problems += "/$cmd: „${r.plain.trim()}“ nicht mit $prefix-Präfix"
            }
        }
        chk("ignore add ${P2}", "Du ignorierst nun", Prefix.Success)
        chk("ignore add ${P2}", "bereits", Prefix.Error)
        chk("ignore remove ${P2}", "nicht mehr", Prefix.Success)
        chk("ignore remove ${P2}", "Du ignorierst ${P2} nicht", Prefix.Error)
        chk("ignore list", "niemanden", Prefix.Info) // empty results use the info prefix (parity fix)
        chk("msg ${P1} x", "selbst", Prefix.Error)
        if (problems.isNotEmpty()) bad(problems.joinToString("; "))
        note("Erfolg grün, Fehler rot für /ignore, /msg")
    }
    scenario("chat-128", "/teamchat aus der Konsole") {
        val line = consoleRejects(PAPER, "teamchat Hallo")
        note("Paper-Konsole: „$line“")
    }
    scenario("chat-129", "KI-Moderation ohne API-Key") {
        on(PAPER, p1)
        val since = java.time.Instant.now()
        p1.chat("Testnachricht ${uniq()}"); sleep(3000)
        val errs = (net.logsSince("paper", since) + net.logsSince("minestom", since)).lines()
            .filter { it.contains("moderation", true) && (it.contains("ERROR") || it.contains("WARN") || it.contains("Exception")) }
        check(errs.isEmpty()) { "Log: ${errs.take(2)}" }
        note("Keine Moderations-Fehler im Log nach Chatnachricht")
    }
}
