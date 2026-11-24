package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Received
import dev.slne.surf.player.tests.bot.Text
import dev.slne.surf.player.tests.dsl.Prefix
import dev.slne.surf.player.tests.infra.Yaml
import dev.slne.surf.player.tests.dsl.notAutomatable
import dev.slne.surf.player.tests.dsl.scenario

private val AUTO_DISABLE: (String) -> String = {
    Yaml.set(Yaml.set(it, "autoDisabling.enabled", "true"), "autoDisabling.maximum-players-before-disable", "1")
}

/** Chat feature (test plan "chat"): ${A} = NotAmmo, ${P1} = FPNS, ${P2} = third account. */
fun registerChat() {
    scenario("chat-01", "Globale Chatnachricht Format") {
        on(PAPER, admin, p1)
        val text = uniq("Hallo")
        val m = admin.mark()
        p1.say(text)
        val r = admin.expect("${P1}", text, since = m)
        check(r.hovers.any { it.second.matches(Regex("""\(\d{2}\.\d{2}\.\d{4}, \d{2}:\d{2}:\d{2}\) $text""")) }) {
            "Hover ohne „(dd.MM.yyyy, HH:mm:ss) $text“: ${r.hovers.map { it.second }}"
        }
        check(r.clicks.any { it.action == "suggest_command" && it.value == "/msg ${P1} " }) { "Klick schlägt nicht „/msg ${P1} “ vor: ${r.clicks.map { it.value }}" }
        note("${A} sieht „${r.plain.trim()}“, Hover mit Zeitstempel, Klick schlägt /msg ${P1} vor")
    }

    scenario("chat-02", "Globaler Chat ist serverlokal") {
        on(PAPER, admin); on(LOBBY, p1)
        val text = uniq("lokal")
        val m = admin.mark()
        p1.say(text)
        admin.expectNone(text, since = m, within = 3000)
        note("${A} (Paper) sieht die Lobby-Nachricht von ${P1} nicht")
    }

    scenario("chat-03", "Löschen-Button für Team") {
        on(PAPER, admin, p1)
        val text = uniq("del")
        val m = admin.mark()
        p1.say(text)
        val r = admin.expect(text, since = m)
        val hover = r.hovers.firstOrNull { it.first.contains("✘") }
            ?: bad("Kein [✘] in „${r.plain.trim()}“")
        check(hover.second.contains("löschen", true)) { "Hover von [✘]: „${hover.second}“" }
        note("${A} sieht „[✘]“ mit Hover „${hover.second}“; Löschen selbst nur mit signierten Nachrichten möglich (Bots senden unsigniert, siehe chat-06)")
    }

    scenario("chat-04", "Kein Löschen-Button ohne Permission") {
        on(PAPER, admin, p1)
        val text = uniq("nodel")
        val m = p1.mark()
        admin.say(text)
        val r = p1.expect(text, since = m)
        check(!r.plain.contains("✘")) { "${P1} sieht ✘: „${r.plain.trim()}“" }
        note("${P1} sieht „${r.plain.trim()}“ ohne [✘]")
    }

    notAutomatable("chat-05", "Benachrichtigung kommt nur nach erfolgreichem Löschen; Bots (Offline-Modus) senden unsignierte Nachrichten, die nicht gelöscht werden können (siehe chat-06).")

    scenario("chat-06", "Löschen ohne Signatur") {
        on(PAPER, admin, p1)
        val text = uniq("unsigned")
        val m = admin.mark()
        p1.say(text)
        val r = admin.expect(text, since = m)
        val m2 = admin.mark()
        admin.clickIn(r, "✘")
        val e = admin.expect("ungültige Signatur", since = m2)
        note("${A} klickt [✘] auf unsignierte Nachricht: „${e.plain.trim()}“")
    }

    // Changed on purpose: clicking the name suggests `/msg <name> ` for everybody; staff teleport
    // with a separate [TP] button in front of the name, which has its own hover text.
    scenario("chat-07", "Teleport per [TP]-Button, Namensklick schlägt /msg vor") {
        on(PAPER, admin, p1)
        val text = uniq("tp")
        val m = admin.mark()
        p1.say(text)
        val r = admin.expect(text, since = m)

        check(r.clicks.any { Text.has(it.text, "${P1}") && it.value == "/msg ${P1} " }) {
            "Klick auf den Namen schlägt nicht „/msg ${P1} “ vor: ${r.clicks.map { it.text to it.value }}"
        }
        val nameHover = r.hovers.firstOrNull { it.second.contains("${P1} eine private Nachricht zu schreiben") }
            ?: bad("Kein Hover „Klicke, um ${P1} eine private Nachricht zu schreiben“ am Namen: ${r.hovers.map { it.second }}")
        val tpHover = r.hovers.firstOrNull { Text.has(it.first, "TP") && it.second.contains("dich zu ${P1} zu teleportieren") }
            ?: bad("Kein [TP]-Button mit Hover „Klicke, um dich zu ${P1} zu teleportieren“: ${r.hovers.map { it.first to it.second }}")

        val before = admin.teleports.get()
        admin.clickIn(r, "TP")
        eventually(5000, "Teleport von ${A} nach Klick auf [TP]") { if (admin.teleports.get() > before) true else null }
        val before2 = admin.teleports.get()
        admin.clickIn(r, "TP")
        eventually(5000, "zweiter Teleport") { if (admin.teleports.get() > before2) true else null }
        note("Name: Hover „${nameHover.second}“, Klick schlägt „/msg ${P1} “ vor; [TP]: Hover „${tpHover.second}“, teleportiert ${A} (zweimal nutzbar)")
    }

    scenario("chat-08", "Kein Teleport ohne Permission") {
        on(PAPER, admin, p1)
        val text = uniq("notp")
        val m = p1.mark()
        admin.say(text)
        val r = p1.expect(text, since = m)
        check(r.hovers.none { it.second.contains("Teleportiere") }) { "${P1} sieht Teleport-Hover" }
        check(r.clicks.any { it.value == "/msg ${A} " }) { "Klick schlägt nicht /msg ${A} vor: ${r.clicks.map { it.value }}" }
        note("Kein Teleport-Hover, Klick schlägt „/msg ${A} “ vor")
    }

    scenario("chat-09", "Erwähnung ohne @") {
        on(PAPER, admin, p1)
        val m = admin.mark()
        p1.say("hi ${A.lowercase()} ${uniq()}")
        val r = admin.expect("@${A}", since = m)
        check(r.clicks.any { it.text.contains("@${A}") && it.value == "/msg ${A} " }) { "Klick auf @${A}: ${r.clicks.map { it.text to it.value }}" }
        note("„${r.plain.trim()}“, @${A} klickbar (/msg ${A})")
    }

    scenario("chat-10", "Erwähnung mit @") {
        on(PAPER, admin, p1)
        val m = admin.mark()
        val t = uniq()
        p1.say("@${A} schau mal $t")
        val r = admin.expect("@${A}", t, since = m)
        check(!r.plain.contains("@@")) { "Doppeltes @: „${r.plain}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("chat-11", "Erwähnung eines abwesenden Spielers") {
        on(LOBBY, admin); on(PAPER, p1, p2)
        val t = uniq()
        val m = p2.mark()
        p1.say("${A.lowercase()} $t")
        val r = p2.expect(t, since = m)
        check(!r.plain.contains("@${A}")) { "Hervorhebung trotz Abwesenheit: „${r.plain}“" }
        note("${P2} sieht „${r.plain.trim()}“ ohne Hervorhebung")
    }

    scenario("chat-12", "Erwähnung eines vanished Spielers") {
        on(PAPER, admin, p1)
        vanish(admin, true)
        try {
            val t = uniq()
            val m1 = p1.mark(); val m2 = admin.mark()
            p1.say("${A} $t")
            val seen1 = p1.expect(t, since = m1)
            val seenA = admin.expect(t, since = m2)
            check(!seen1.plain.contains("@${A}")) { "${P1} sieht Hervorhebung des vanished ${A}: „${seen1.plain}“" }
            check(seenA.plain.contains("@${A}")) { "${A} sieht keine Hervorhebung: „${seenA.plain}“" }
            note("${P1}: „${seen1.plain.trim()}“, ${A}: „${seenA.plain.trim()}“")
        } finally {
            vanish(admin, false)
        }
    }

    scenario("chat-13", "[item] mit Item") {
        on(PAPER, p1, p2)
        net.console(PAPER, "clear ${P1}", 500)
        net.console(PAPER, "give ${P1} diamond 3", 1000)
        p1.selectHotbar(0)
        sleep(500)
        val m = p2.mark()
        p1.say("schau [item] ${uniq()}")
        val r = p2.expect("3x", since = m)
        check(!r.plain.contains("[item]")) { "[item] nicht ersetzt: „${r.plain}“" }
        val m2 = p2.mark()
        p1.say("und [ITEM] ${uniq()}")
        val r2 = p2.expect("3x", since = m2)
        net.console(PAPER, "clear ${P1}", 500)
        note("„${r.plain.trim()}“ / „${r2.plain.trim()}“ (Item-Name als Übersetzungsschlüssel, vom Client lokalisiert)")
    }

    scenario("chat-14", "[item] ohne Item") {
        on(PAPER, p1)
        net.console(PAPER, "clear ${P1}", 800)
        val m = p1.say("[item] ${uniq()}")
        val r = p1.expect("Du hast kein Item in der Hand!", since = m, prefix = Prefix.Error)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-15", "[item] per Config deaktiviert") {
        on(PAPER, p1, p2)
        withConfig(PAPER, "chat/config.yml", { Yaml.set(it, "item-placeholder", "false") }, { reloadOn(PAPER, "chat") }) {
            net.console(PAPER, "give ${P1} diamond 3", 800)
            val t = uniq()
            val m = p2.mark()
            p1.say("[item] $t")
            val r = p2.expect(t, since = m)
            check(r.plain.contains("[item]")) { "[item] wurde trotz item-placeholder: false ersetzt: „${r.plain}“" }
            note("Mit item-placeholder: false bleibt „[item]“ stehen")
        }
        net.console(PAPER, "clear ${P1}", 500)
    }

    scenario("chat-16", "[item] auf Minestom") {
        on(LOBBY, p1, p2)
        val t = uniq()
        val m = p2.mark()
        p1.say("[item] $t")
        val r = p2.expect(t, since = m)
        check(r.plain.contains("[item]")) { "Ersetzt auf Minestom: „${r.plain}“" }
        note("Lobby: „${r.plain.trim()}“ unverändert")
    }

    scenario("chat-17", "Erlaubter Link klickbar") {
        on(PAPER, p1, p2)
        val m = p2.mark()
        val t = uniq()
        p1.say("castcrafter.de $t")
        val r = p2.expect("castcrafter.de", t, since = m)
        val link = r.clicks.firstOrNull { it.action == "open_url" } ?: bad("Link nicht klickbar: ${r.clicks}")
        check(link.value.startsWith("https://castcrafter.de")) { "URL ${link.value}" }
        val hover = r.hovers.firstOrNull { it.second.contains("Link zu öffnen") } ?: bad("Hover fehlt: ${r.hovers}")
        note("Klick öffnet ${link.value}, Hover „${hover.second}“")
    }

    scenario("chat-18", "Unerlaubter Link blockiert") {
        on(PAPER, p1, p2)
        val t = uniq()
        val m2 = p2.mark()
        val m = p1.say("google.com $t")
        val r = p1.expect("unerlaubten Link", since = m, prefix = Prefix.Error)
        p2.expectNone(t, since = m2, within = 1500)
        note("„${r.plain.trim()}“, nicht gesendet")
    }

    scenario("chat-19", "Erlaubte Domains per Config") {
        on(PAPER, p1, p2)
        withConfig(PAPER, "chat/config.yml", { Yaml.addListItem(it, "allowed-domains", "example.org") }, { reloadOn(PAPER, "chat") }) {
            val t = uniq()
            val m = p2.mark()
            p1.say("example.org $t")
            p2.expect("example.org", t, since = m)
            note("example.org nach Config-Erweiterung gesendet")
        }
    }

    scenario("chat-20", "Unerlaubte Zeichen") {
        on(PAPER, p1)
        val m = p1.say("Привет ${uniq()}")
        val r = p1.expect("unerlaubte Zeichen", since = m)
        val m2 = p1.say("äöü ß € ${uniq()}")
        p1.expectNone("unerlaubte Zeichen", since = m2, within = 1500)
        note("Kyrillisch: „${r.plain.trim()}“; äöüß€ erlaubt")
    }

    scenario("chat-21", "Spam-Schutz Rate-Limit") {
        on(PAPER, p1, p2)
        sleep(3500)
        val m = p1.mark(); val m2 = p2.mark()
        val texts = (1..6).map { uniq("spam$it") }
        texts.forEach { p1.chat(it); sleep(250) }
        val r = p1.expect("zu schnell", since = m)
        sleep(800)
        val delivered = texts.count { t -> p2.since(m2).any { it.plain.contains(t) } }
        val blocked = p1.since(m).count { it.plain.contains("zu schnell") }
        note("6 Nachrichten in <3 s: $delivered zugestellt, $blocked× „${r.plain.trim()}“")
        sleep(3500)
        check(delivered == 5 && blocked == 1) { "Erwartet 5 zugestellt/1 blockiert; ${notes.last()}" }
    }

    scenario("chat-22", "Wiederholte Nachricht") {
        on(PAPER, p1)
        sleep(3500)
        val t = uniq("test")
        p1.chat(t)
        sleep(700)
        val m = p1.say(t.uppercase().replace("TEST", "test").let { "TEST" + t.removePrefix("test") })
        val r = p1.expect("zweimal hintereinander", since = m)
        note("„${r.plain.trim()}“")
        sleep(3500)
    }

    scenario("chat-23", "Zu viele Großbuchstaben") {
        on(PAPER, p1)
        sleep(3500)
        val m2 = p1.say("HALLO ${uniq()}")
        p1.expectNone("Großbuchstaben", since = m2, within = 1500)
        sleep(1000)
        val m = p1.say("HALLOO ${uniq()}")
        val r = p1.expect("Großbuchstaben", since = m)
        note("„HALLO“ geht durch; „HALLOO“: „${r.plain.trim()}“")
    }

    scenario("chat-24", "Filter-Bypass") {
        on(PAPER, admin, p1)
        val t = uniq()
        val m = p1.mark()
        admin.chat("HALLOOO google.com 😀 $t")
        p1.expect(t, since = m)
        val m2 = p1.mark()
        val ts = (1..7).map { uniq("b") }
        ts.forEach { admin.chat(it); sleep(150) }
        sleep(1500)
        val got = ts.count { t2 -> p1.since(m2).any { it.plain.contains(t2) } }
        check(got == 7) { "Nur $got von 7 schnellen Nachrichten kamen an" }
        note("Großbuchstaben, Link, Emoji und 7 schnelle Nachrichten von ${A} zugestellt")
    }

    scenario("chat-25", "Auto-Disabling bei vielen Spielern") {
        on(PAPER, admin, p1)
        withConfig(PAPER, "chat/config.yml", AUTO_DISABLE, { reloadOn(PAPER, "chat") }) {
            val m = p1.say(uniq())
            val r = p1.expect("nicht schreiben", since = m)
            note("„${r.plain.trim()}“")
        }
    }

    scenario("chat-26", "Auto-Disabling Bypass") {
        on(PAPER, admin, p1)
        withConfig(PAPER, "chat/config.yml", AUTO_DISABLE, { reloadOn(PAPER, "chat") }) {
            val t = uniq()
            val m = p1.mark()
            admin.chat(t)
            p1.expect(t, since = m)
            note("${A} kann trotz Auto-Disabling schreiben")
        }
    }

    scenario("chat-27", "Chat deaktiviert (Functionality)") {
        on(PAPER, admin, p1)
        admin.cmd("surfplayer chat toggle aus")
        sleep(1500)
        try {
            val m = p1.say(uniq())
            val r = p1.expect("deaktiviert", since = m)
            note("„${r.plain.trim()}“")
        } finally {
            admin.cmd("surfplayer chat toggle an"); sleep(1500)
        }
    }

    scenario("chat-28", "Functionality-Bypass") {
        on(PAPER, admin, p1)
        admin.cmd("surfplayer chat toggle aus")
        sleep(1500)
        try {
            val t = uniq()
            val m = p1.mark()
            admin.chat(t)
            p1.expect(t, since = m)
            note("${A} schreibt trotz deaktiviertem Chat")
        } finally {
            admin.cmd("surfplayer chat toggle an"); sleep(1500)
        }
    }

    scenario("chat-29", "Ignorierter Sender im globalen Chat") {
        // team members (DevAdmin) cannot be ignored (IgnoreRules), so DevPlayer2 is the ignored sender
        on(PAPER, p2, p1)
        resetIgnores(p1)
        p1.cmd("ignore add ${P2}"); sleep(1500)
        try {
            val t = uniq()
            val m = p1.mark()
            p2.chat(t)
            p1.expectNone(t, since = m, within = 3000)
            note("${P1} sieht die Nachricht des ignorierten ${P2} nicht (DevAdmin als Teammitglied nicht ignorierbar)")
        } finally {
            p1.cmd("ignore remove ${P2}"); sleep(1000)
        }
    }

    scenario("chat-30", "Join-Nachricht") {
        on(PAPER, admin); on(LOBBY, p1)
        val m = admin.mark()
        on(PAPER, p1)
        val r = admin.expect("[+]", "${P1}", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-31", "Quit-Nachricht") {
        on(PAPER, admin, p1)
        val m = admin.mark()
        on(LOBBY, p1)
        val r = admin.expect("[-]", "${P1}", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-32", "Verbindungsnachrichten per Config aus") {
        // As in surf-chat, disabled connection messages reach nobody, not even always-show holders.
        on(PAPER, admin, p2); on(LOBBY, p1)
        sleep(1500) // a quit message of DevPlayer1 leaving Paper just now may still be on its way
        withConfig(PAPER, "chat/config.yml", { Yaml.set(it, "connection-message-config.enabled", "false") }, { reloadOn(PAPER, "chat") }) {
            val m = p2.mark()
            val ma = admin.mark()
            on(PAPER, p1)
            on(LOBBY, p1)
            p2.expectNone("${P1}", since = m, within = 2000)
            admin.expectNone("${P1}", since = ma, within = 500)
            note("Weder ${P2} noch ${A} sehen eine Join-/Quit-Nachricht (auch keine Vanilla-Nachricht)")
        }
    }

    scenario("chat-33", "Always-Show-Permission") {
        on(PAPER, p1); on(LOBBY, admin)
        setSetting(p1, "chat:connection-messages", false)
        try {
            val m = p1.mark()
            on(PAPER, admin)
            val r = p1.expect("[+]", "${A}", since = m)
            note("${P1} (Verbindungsnachrichten aus) sieht „${r.plain.trim()}“ dank always-show von ${A}")
        } finally {
            setSetting(p1, "chat:connection-messages", true)
        }
    }

    scenario("chat-34", "Join-MOTD") {
        on(LOBBY, p1)
        val m = p1.mark()
        on(PAPER, p1)
        val r = p1.expect("Willkommen auf dem CastCrafter Community Server!", since = m)
        check(r.plain.contains("https://castcrafter.de/server")) { "Link fehlt: ${r.plain}" }
        check(r.plain.contains("-----")) { "Trennlinien fehlen" }
        note("MOTD auf Paper mit Trennlinien und Link")
    }

    scenario("chat-35", "MOTD deaktiviert") {
        on(LOBBY, p1)
        withConfig(PAPER, "chat/config.yml", { Yaml.set(it, "joinMotd.enabled", "false") }, { reloadOn(PAPER, "chat") }) {
            val m = p1.mark()
            on(PAPER, p1)
            p1.expectNone("Willkommen", since = m, within = 2500)
            note("Keine MOTD bei joinMotd.enabled: false")
        }
    }

    scenario("chat-36", "Vanished Join ohne Join-Nachricht") {
        on(PAPER, p1)
        vanish(admin, true)
        try {
            on(LOBBY, admin)
            val m = p1.mark()
            on(PAPER, admin)
            p1.expectNone("[+]", "${A}", since = m, within = 3000)
            note("Keine [+]-Nachricht für den vanished ${A}")
        } finally {
            vanish(admin, false)
        }
    }

    scenario("chat-37", "/msg gleicher Server") {
        on(PAPER, admin, p1)
        val t = uniq("Hallo")
        val ma = admin.mark()
        val m = p1.run("msg ${A} $t")
        val out = p1.expect("PM", "Du -> ${A}", t, since = m)
        val inc = admin.expect("PM", "${P1} -> Dir", t, since = ma)
        val hover = inc.hovers.firstOrNull()?.second ?: bad("Kein Hover an der PN")
        check(hover.contains("von") && hover.contains("am") && hover.contains("auf")) { "Hover: $hover" }
        check(inc.clicks.any { it.value == "/msg ${P1} " }) { "Klick: ${inc.clicks.map { it.value }}" }
        note("${P1}: „${out.plain.trim()}“; ${A}: „${inc.plain.trim()}“; Hover „${hover.replace("\n", " / ")}“")
    }

    scenario("chat-38", "/msg Aliase") {
        on(PAPER, admin, p1)
        val missing = mutableListOf<String>()
        for (alias in listOf("dm", "w", "whisper", "tell", "pm")) {
            val t = uniq(alias)
            val m = admin.mark()
            p1.cmd("$alias ${A} $t")
            if (admin.await(4000, m) { it.plain.contains(t) && it.plain.contains("Dir") } == null) missing += alias
        }
        check(missing.isEmpty()) { "Kein PN-Empfang über /${missing.joinToString(", /")}" }
        note("/dm, /w, /whisper, /tell, /pm stellen zu")
    }

    scenario("chat-39", "/msg an sich selbst") {
        on(PAPER, p1)
        val m = p1.run("msg ${P1} Hallo")
        val r = p1.expect("selbst keine privaten Nachrichten", since = m, prefix = Prefix.Error)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-40", "/msg an Offline-Spieler") {
        on(PAPER, p1)
        quit(p2)
        val m = p1.run("msg ${P2} x")
        val r = p1.firstMessage(m, 5000) ?: bad("Keine Antwort")
        check(!r.plain.contains("-> ${P2}")) { "Nachricht an Offline-Spieler gesendet: ${r.plain}" }
        note("Fehler: „${r.plain.trim().replace("\n", " ")}“")
    }

    scenario("chat-41", "/msg Paper → Minestom") {
        on(PAPER, p1); on(LOBBY, admin)
        val t = uniq()
        val ma = admin.mark()
        val m = p1.run("msg ${A} $t")
        p1.expect("Du -> ${A}", t, since = m)
        admin.expect("${P1} -> Dir", t, since = ma)
        note("${A} erhält die PN auf Minestom, ${P1} sieht die ausgehende")
    }

    scenario("chat-42", "/msg Minestom → Paper") {
        on(LOBBY, p1); on(PAPER, admin)
        val t = uniq()
        val ma = admin.mark()
        p1.cmd("msg ${A} $t")
        admin.expect("${P1} -> Dir", t, since = ma)
        note("${A} (Paper) erhält die PN aus der Lobby")
    }

    scenario("chat-43", "/msg an Ignorierenden") {
        on(PAPER, admin, p1)
        resetIgnores(admin)
        admin.cmd("ignore add ${P1}"); sleep(1500)
        try {
            val t = uniq()
            val ma = admin.mark()
            val m = p1.run("msg ${A} $t")
            val r = p1.expect("nicht zugestellt", since = m)
            admin.expectNone(t, since = ma, within = 2000)
            note("„${r.plain.trim()}“; ${A} erhält nichts")
        } finally {
            admin.cmd("ignore remove ${P1}"); sleep(1000)
        }
    }

    scenario("chat-44", "/msg wird gefiltert") {
        on(PAPER, admin, p1)
        sleep(3000)
        val ma = admin.mark()
        val m = p1.run("msg ${A} HALLO google.com")
        val first = p1.firstMessage(m, 5000) ?: bad("Keine Antwort")
        sleep(1500)
        admin.expectNone("google.com", since = ma, within = 500)
        val all = p1.messagesSince(m).joinToString(" | ") { it.plain.trim() }
        check(all.contains("nicht zugestellt") || all.contains("Großbuchstaben") || all.contains("Link")) { "Antwort: $all" }
        note("${P1} erhält: $all; nicht zugestellt (erste Meldung „${first.plain.trim()}“)")
    }

    scenario("chat-45", "/reply ohne vorherige PN") {
        on(PAPER, p2)
        val m = p2.run("r Hallo")
        val r = p2.expect("noch keine privaten Nachrichten", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-46", "/reply und /r") {
        on(PAPER, admin, p1)
        admin.cmd("msg ${P1} Hi ${uniq()}")
        sleep(1500)
        val t1 = uniq("Danke"); val t2 = uniq("Danke2")
        val ma = admin.mark()
        p1.cmd("reply $t1"); sleep(500)
        p1.cmd("r $t2")
        admin.expect(t1, since = ma)
        admin.expect(t2, since = ma)
        note("/reply und /r gehen an ${A}")
    }

    scenario("chat-47", "/reply Ziel offline") {
        on(PAPER, p1, p2)
        p2.cmd("msg ${P1} Hi ${uniq()}")
        sleep(1500)
        quit(p2)
        val m = p1.run("r Hallo")
        val r = p1.firstMessage(m, 5000) ?: bad("Keine Antwort")
        check(r.plain.contains("Der Spieler ist nicht online.")) { "Antwort: „${r.plain.trim()}“ (erwartet „Der Spieler ist nicht online.“)" }
        if (!hasPrefix(r, Prefix.Error)) odd("„${r.plain.trim()}“ ohne Fehler-Präfix")
        note("„${r.plain.trim()}“")
    }

    scenario("chat-48", "/reply serverübergreifend") {
        on(LOBBY, admin); on(PAPER, p1)
        admin.cmd("msg ${P1} Hi ${uniq()}")
        sleep(1500)
        val t = uniq()
        val ma = admin.mark()
        p1.cmd("r $t")
        admin.expect(t, since = ma)
        note("Antwort von Paper kommt in der Lobby an")
    }

    scenario("chat-49", "/reply läuft nach 15 Minuten ab") {
        on(PAPER, admin, p1)
        admin.cmd("msg ${P1} Hi ${uniq()}")
        sleep(1500)
        val keys = net.redis("--scan", "--pattern", "*reply*").lines().filter { it.isNotBlank() }
        val ttl = keys.mapNotNull { k -> net.redis("TTL", k).trim().toLongOrNull()?.let { k to it } }
        val hit = ttl.firstOrNull { it.second in 1..900 }
        if (hit != null) {
            note("Redis-Schlüssel ${hit.first} hat TTL ${hit.second}s (≤ 15 min); Ablauf selbst nicht abgewartet")
        } else {
            skip("Ablauf nach 15 min nicht abgewartet; Reply-Cache in Redis nicht mit TTL gefunden (Schlüssel: $keys, TTL: $ttl)")
        }
    }

    scenario("chat-50", "/teamchat") {
        on(PAPER, admin, p1)
        val t = uniq("Hallo")
        val m = admin.mark(); val m1 = p1.mark()
        admin.cmd("teamchat $t")
        val r = admin.expect("TEAM", "${A}", t, since = m)
        check(r.clicks.any { it.value == "/teamchat " }) { "Klick: ${r.clicks.map { it.value }}" }
        p1.expectNone(t, since = m1, within = 1500)
        note("„${r.plain.trim()}“, Klick schlägt /teamchat vor; ${P1} sieht nichts")
    }

    scenario("chat-51", "/tc Alias") {
        on(PAPER, admin)
        val t = uniq()
        val m = admin.run("tc $t")
        admin.expect("TEAM", t, since = m)
        note("/tc funktioniert")
    }

    scenario("chat-52", "/teamchat ohne Permission") {
        on(PAPER, p1)
        p1.expectCommandUnavailable("teamchat")
        p1.expectCommandUnavailable("tc")
        note("/teamchat und /tc nicht im Befehlsbaum von ${P1}")
    }

    scenario("chat-53", "Teamchat serverübergreifend") {
        on(PAPER, admin); on(LOBBY, p2)
        withPermission(p2, "surf.player.team.teamchat") {
            val t = uniq()
            val m = p2.mark()
            admin.cmd("tc $t")
            val r = p2.expect("TEAM", t, since = m)
            note("${P2} (Teamchat-Recht, Lobby) sieht „${r.plain.trim()}“")
        }
    }

    scenario("chat-54", "/ignore Spieler") {
        on(PAPER, p1)
        resetIgnores(p1)
        val m = p1.run("ignore add ${P2}")
        val r = p1.expect("Du ignorierst nun ${P2}", since = m, prefix = Prefix.Success)
        val m2 = p1.run("ignore add ${A}")
        val team = p1.firstMessage(m2)
        p1.cmd("ignore remove ${P2}"); sleep(500)
        note("„${r.plain.trim()}“ (DevPlayer2); DevAdmin als Teammitglied: „${team?.plain?.trim()}“")
    }

    scenario("chat-55", "/ignore remove hebt auf") {
        on(PAPER, p1)
        resetIgnores(p1)
        p1.cmd("ignore add ${P2}"); sleep(1000)
        val m = p1.run("ignore remove ${P2}")
        val r = p1.expect("Du ignorierst ${P2} nicht mehr", since = m, prefix = Prefix.Success)
        note("„${r.plain.trim()}“ (Toggle per /ignore ersetzt durch /ignore remove)")
    }

    scenario("chat-56", "/ignore sich selbst") {
        on(PAPER, p1)
        val m = p1.run("ignore add ${P1}")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht selbst ignorieren")) { "Antwort: „${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("chat-57", "/ignore unbekannter Spieler") {
        on(PAPER, p1)
        val m = p1.run("ignore add NichtExistent123")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht gefunden")) { "Antwort: „${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("chat-58", "/ignore Offline-Spieler") {
        on(PAPER, p1)
        resetIgnores(p1)
        quit(p2)
        try {
            val m = p1.run("ignore add ${P2}")
            val r = p1.firstMessage(m) ?: bad("Keine Antwort")
            check(r.plain.contains("Du ignorierst nun ${P2}")) { "Antwort: „${r.plain.trim()}“" }
            note("„${r.plain.trim()}“")
        } finally {
            p1.cmd("ignore remove ${P2}"); sleep(800)
        }
    }

    scenario("chat-59", "/ignore entfernt Freundschaftsanfragen") {
        on(PAPER, p2, p1)
        resetFriends(); resetIgnores(p1)
        p2.cmd("friend add ${P1}"); sleep(1500)
        p1.cmd("ignore add ${P2}"); sleep(1500)
        try {
            val m = p1.run("friend requests")
            val r = p1.firstMessage(m) ?: bad("Keine Antwort")
            check(!r.plain.contains("${P2}")) { "Anfrage von ${P2} noch offen: „${r.plain.trim()}“" }
            val rows = net.sql("""SELECT count(*) FROM "surf-friends".friend_requests WHERE (sender_uuid = '${p2.uuid}' AND target_uuid = '${p1.uuid}') OR (sender_uuid = '${p1.uuid}' AND target_uuid = '${p2.uuid}')""")
            check(rows[0][0] == "0") { "friend_requests enthält noch ${rows[0][0]} Zeile(n)" }
            note("Nach /ignore add ${P2}: „${r.plain.trim()}“, keine Anfrage in der DB")
        } finally {
            p1.cmd("ignore remove ${P2}"); sleep(800); resetFriends()
        }
    }

    scenario("chat-60", "/ignore list") {
        on(PAPER, p1)
        resetIgnores(p1)
        p1.cmd("ignore add ${P2}"); sleep(1200)
        try {
            val m = p1.run("ignore list")
            val r = p1.expect("${P2}", since = m)
            check(p1.messagesSince(m).any { Text.has(it.plain, "Ignorierte Spieler") }) { "Überschrift fehlt: „${r.plain.trim()}“" }
            check(Regex("""\d{2}\.\d{2}\.\d{4}""").containsMatchIn(r.plain)) { "Kein Datum: „${r.plain.trim()}“" }
            note("„${r.plain.trim().replace("\n", " ⏎ ")}“")
        } finally {
            p1.cmd("ignore remove ${P2}"); sleep(800)
        }
    }

    scenario("chat-61", "/ignore list leer") {
        resetIgnores(p1)
        val results = mutableListOf<String>()
        for (server in listOf(PAPER, LOBBY)) {
            on(server, p1)
            val m = p1.run("ignore list")
            val r = p1.firstMessage(m) ?: bad("$server: keine Antwort")
            check(r.plain.contains("niemanden")) { "$server: „${r.plain.trim()}“" }
            if (!hasPrefix(r, Prefix.Error)) odd("$server: „${r.plain.trim()}“ ohne Fehler-Präfix")
            results += "$server: „${r.plain.trim()}“"
        }
        note(results.joinToString("; "))
    }

    scenario("chat-62", "Ignore übergreifend/persistent") {
        on(PAPER, p2, p1)
        resetIgnores(p1)
        p1.cmd("ignore add ${P2}"); sleep(1500)
        try {
            val rows = net.sql("""SELECT count(*) FROM "surf-chat".chat_ignorelist_entries WHERE user_id = '${p1.uuid}' AND target_uuid = '${p2.uuid}'""")
            check(rows[0][0] == "1") { "DB-Zeilen: ${rows[0][0]}" }
            on(LOBBY, p2, p1)
            val t = uniq()
            val m = p1.mark()
            p2.chat(t)
            p1.expectNone(t, since = m, within = 3000)
            note("DB-Zeile vorhanden; in der Lobby sieht ${P1} ${P2}s Nachricht nicht")
        } finally {
            p1.cmd("ignore remove ${P2}"); sleep(800)
        }
    }

    scenario("chat-63", "/spy aktivieren") {
        on(PAPER, admin, p1)
        admin.cmd("spy clear"); sleep(800)
        val m = admin.run("spy add ${P1}")
        val r = admin.expect("Du liest nun die privaten Nachrichten von ${P1} mit", since = m, prefix = Prefix.Success)
        note("„${r.plain.trim()}“")
        admin.cmd("spy clear"); sleep(500)
    }

    scenario("chat-64", "/spy Ausgabe") {
        on(PAPER, admin, p1, p2)
        admin.cmd("spy clear"); sleep(500)
        admin.cmd("spy add ${P1}"); sleep(1000)
        try {
            val t = uniq()
            val m = admin.mark()
            p1.cmd("msg ${P2} $t")
            val r = admin.expect("SPY", t, since = m)
            check(r.plain.contains("[TP]")) { "Kein [TP] obwohl ${A} surf.player.chat.teleport hat: „${r.plain.trim()}“" }
            note("„${r.plain.trim()}“")
        } finally {
            admin.cmd("spy clear"); sleep(500)
        }
    }

    scenario("chat-65", "/spy deaktivieren") {
        on(PAPER, admin, p1)
        admin.cmd("spy clear"); sleep(500)
        admin.cmd("spy add ${P1}"); sleep(800)
        val m = admin.run("spy remove ${P1}")
        val r = admin.expect("Du liest die privaten Nachrichten von ${P1} nicht mehr mit", since = m, prefix = Prefix.Success)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-66", "/spy sich selbst") {
        on(PAPER, admin)
        val m = admin.run("spy add ${A}")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("Du kannst dich nicht selbst ausspionieren")) { "„${r.plain.trim()}“" }
        note("Paper: „${r.plain.trim()}“")
        on(LOBBY, admin)
        val m2 = admin.run("spy add ${A}")
        val r2 = admin.firstMessage(m2) ?: bad("Keine Antwort auf Minestom")
        check(r2.plain.contains("nicht selbst ausspionieren")) { "Minestom: „${r2.plain.trim()}“" }
        val m3 = admin.run("spy remove ${A}"); admin.firstMessage(m3, 2000)
        note("Minestom: „${r2.plain.trim()}“")
    }

    scenario("chat-67", "/spy auf Teammitglied") {
        on(PAPER, admin, p2)
        withPermission(p2, "surf.player.chat.bypass.spy") {
            val m = admin.run("spy add ${P2}")
            val r = admin.firstMessage(m) ?: bad("Keine Antwort")
            check(r.plain.contains("Du kannst keine Teammitglieder ausspionieren")) { "„${r.plain.trim()}“" }
            note("„${r.plain.trim()}“")
        }
    }

    scenario("chat-68", "/spy clear") {
        on(PAPER, admin, p1)
        admin.cmd("spy add ${P1}"); sleep(800)
        val m = admin.run("spy clear")
        val r = admin.expect("Du liest bei keinem Spieler mehr mit", since = m, prefix = Prefix.Success)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-69", "/spy clear ohne Spy") {
        on(PAPER, admin)
        admin.cmd("spy clear"); sleep(800)
        val m = admin.run("spy clear")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("Du liest bei keinem Spieler mit")) { "„${r.plain.trim()}“" }
        if (!hasPrefix(r, Prefix.Error)) odd("„${r.plain.trim()}“ ohne Fehler-Präfix")
        note("„${r.plain.trim()}“")
    }

    scenario("chat-70", "Spy endet beim Quit") {
        on(PAPER, admin, p1, p2)
        admin.cmd("spy clear"); sleep(500)
        admin.cmd("spy add ${P1}"); sleep(800)
        quit(admin)
        on(PAPER, admin)
        val t = uniq()
        val m = admin.mark()
        p1.cmd("msg ${P2} $t")
        admin.expectNone("SPY", since = m, within = 3000)
        note("Nach Rejoin keine [SPY]-Zeile mehr")
    }

    scenario("chat-71", "/slowchat info") {
        on(PAPER, admin)
        val m = admin.run("slowchat info")
        val r = admin.expect("Slowchat ist derzeit", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-72", "/slowchat toggle an") {
        on(PAPER, admin, p1)
        admin.cmd("slowchat toggle aus"); sleep(1000)
        val m1 = p1.mark()
        val m = admin.run("slowchat toggle an")
        try {
            val r = admin.expect("Der Slowchat wurde aktiviert", since = m, prefix = Prefix.Success)
            val box = p1.expect("Slow Chat wurde aktiviert", since = m1)
            val notify = admin.messagesSince(m).firstOrNull { it.plain.contains("Der Slowchat wurde von ${A} aktiviert") }
            note("${A}: „${r.plain.trim()}“; alle: „${box.plain.trim().replace("\n", " ")}“; Team-Info: ${notify?.plain?.trim() ?: "keine"}")
            if (notify == null) odd("Keine Team-Benachrichtigung „Der Slowchat wurde von ${A} aktiviert.“ an ${A}; ${notes.last()}")
        } finally {
            admin.cmd("slowchat toggle aus"); sleep(1000)
        }
    }

    scenario("chat-73", "/slowchat bereits aktiv") {
        on(PAPER, admin)
        admin.cmd("slowchat toggle an"); sleep(1000)
        try {
            val m = admin.run("slowchat toggle an")
            val r = admin.expect("Der Slowchat ist bereits aktiviert", since = m, prefix = Prefix.Error)
            note("„${r.plain.trim()}“")
        } finally {
            admin.cmd("slowchat toggle aus"); sleep(1000)
        }
    }

    scenario("chat-74", "Slowchat Cooldown") {
        on(PAPER, admin, p1)
        admin.cmd("slowchat toggle an"); sleep(1000)
        try {
            sleep(3000)
            p1.chat(uniq("a")); sleep(1200)
            val m = p1.say(uniq("b"))
            val r = p1.expect("Bitte warte einen Moment", since = m)
            note("„${r.plain.trim()}“")
        } finally {
            admin.cmd("slowchat toggle aus"); sleep(1000)
        }
    }

    scenario("chat-75", "Slowchat Bypass") {
        on(PAPER, admin, p1)
        admin.cmd("slowchat toggle an"); sleep(1000)
        try {
            val m = admin.mark()
            val a = uniq("a"); val b = uniq("b")
            val mp = p1.mark()
            admin.chat(a); sleep(1200); admin.chat(b)
            p1.expect(b, since = mp)
            admin.expectNone("Bitte warte", since = m, within = 500)
            note("${A} schreibt mehrfach ohne Cooldown")
        } finally {
            admin.cmd("slowchat toggle aus"); sleep(1000)
        }
    }

    scenario("chat-76", "/slowchat toggle aus") {
        on(PAPER, admin, p1)
        admin.cmd("slowchat toggle an"); sleep(1000)
        val m1 = p1.mark()
        val m = admin.run("slowchat toggle aus")
        val r = admin.expect("Der Slowchat wurde deaktiviert", since = m, prefix = Prefix.Success)
        val box = p1.expect("Slow Chat wurde deaktiviert", since = m1)
        note("„${r.plain.trim()}“; alle: „${box.plain.trim().replace("\n", " ")}“")
    }

    scenario("chat-77", "/slowchat ungültiges Argument") {
        on(PAPER, admin)
        val m = admin.run("slowchat toggle vielleicht")
        val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        val sugg = admin.suggestions("/slowchat toggle ")
        note("„${r.plain.trim().replace("\n", " ")}“; Vorschläge $sugg")
        check(r.plain.contains("Bitte gib 'an' oder 'aus' an.")) { "Erwartet „Bitte gib 'an' oder 'aus' an.“; ${notes.last()}" }
        if (!hasPrefix(r, Prefix.Error)) odd("Ohne Fehler-Präfix; ${notes.last()}")
    }

    scenario("chat-78", "Slowchat nur lokal") {
        on(PAPER, admin); on(LOBBY, p1)
        admin.cmd("slowchat toggle an"); sleep(1000)
        try {
            sleep(3000)
            p1.chat(uniq("a")); sleep(1200)
            val m = p1.say(uniq("b"))
            p1.expectNone("Bitte warte", since = m, within = 2000)
            note("Kein Cooldown in der Lobby bei Slowchat auf Paper")
        } finally {
            admin.cmd("slowchat toggle aus"); sleep(1000)
        }
    }

    scenario("chat-80", "/surfplayer chat filters") {
        on(PAPER, admin)
        val m = admin.run("surfplayer chat filters")
        sleep(1500)
        admin.cmd("surfplayer chat filters 2")
        sleep(1500)
        val all = admin.messagesSince(m).joinToString("\n") { it.plain }
        val missing = listOf("--type", "--range", "--message", "--deletedBy", "--deleted", "--limit", "--page", "--server", "--sender", "--receiver", "--messageUuid").filter { !all.contains(it) }
        check(all.isNotBlank()) { "Keine Antwort" }
        check(missing.isEmpty()) { "Filter fehlen: $missing in „${all.take(300)}“" }
        note("Filterhilfe nennt alle 11 Filter")
    }

    scenario("chat-81", "/surfplayer chat lookup ohne Filter") {
        on(PAPER, admin, p1)
        p1.chat(uniq("lookup")); sleep(1000)
        val m = admin.run("surfplayer chat lookup")
        admin.expect("Es wird nach Ergebnissen gesucht", since = m)
        val r = admin.expect("Suchergebnisse", since = m, timeout = 12000)
        note("„${r.plain.trim().lines().take(3).joinToString(" ⏎ ")}…“")
    }

    /** [rawArgs] may use @A / @P1 for the group's admin / player 1. */
    fun lookup(id: String, title: String, rawArgs: String, prepare: dev.slne.surf.player.tests.dsl.Ctx.() -> Unit = {}, verify: dev.slne.surf.player.tests.dsl.Ctx.(String) -> String?) =
        scenario(id, title) {
            val args = rawArgs.replace("@P1", P1).replace("@A", A)
            on(PAPER, admin, p1)
            prepare()
            val m = admin.run("surfplayer chat lookup $args")
            val r = admin.await(12000, m) { Text.has(it.plain, "Suchergebnisse") || it.plain.contains("keine Ergebnisse") || it.plain.contains("abgebrochen") }
                ?: bad("Keine Suchergebnisse für „$args“: ${observed(admin, m)}")
            val problem = verify(r.plain)
            if (problem != null) bad("$args: $problem — „${r.plain.trim().take(400)}“")
            note("„$args“: „${r.plain.trim().lines().take(4).joinToString(" ⏎ ").take(300)}“")
        }

    lookup("chat-82", "lookup --type", "--type team", { admin.cmd("tc ${uniq()}"); sleep(1000) }) { if (Text.has(it, "Suchergebnisse")) null else "keine Ergebnisse" }
    lookup("chat-83", "lookup --range", "--range 10m", { p1.chat(uniq()); sleep(1000) }) { if (Text.has(it, "Suchergebnisse")) null else "keine Ergebnisse" }
    lookup("chat-84", "lookup --sender", "--sender @P1 --limit 3", { p1.chat(uniq()); sleep(1000) }) { t ->
        val rows = t.lines().filter { it.contains("schrieb") }
        when {
            rows.isEmpty() -> "keine Treffer"
            rows.size > 3 -> "${rows.size} Treffer trotz --limit 3"
            !rows.all { it.contains("${P1}") } -> "Ergebnis enthält andere Absender"
            else -> null
        }
    }
    lookup("chat-85", "lookup --receiver", "--receiver @A", { p1.cmd("msg ${A} ${uniq()}"); sleep(1000) }) { if (Text.has(it, "Suchergebnisse")) null else "keine Ergebnisse" }
    lookup("chat-86", "lookup --server", "--server paper", { p1.chat(uniq()); sleep(1000) }) { if (Text.has(it, "Suchergebnisse")) null else "keine Ergebnisse" }
    lookup("chat-87", "lookup --deleted / --deletedBy", "--deleted true") { null }
    lookup("chat-88", "lookup --limit / --page", "--limit 20 --page 2") { null }
    scenario("chat-89", "lookup --messageUuid") {
        val rows = net.sql("""SELECT message_uuid FROM "surf-chat".chat_histories ORDER BY id DESC LIMIT 1""")
        val uuid = rows.firstOrNull()?.firstOrNull() ?: skip("Keine Chat-History vorhanden")
        on(PAPER, admin)
        val m = admin.run("surfplayer chat lookup --messageUuid $uuid")
        val r = admin.await(12000, m) { Text.has(it.plain, "Suchergebnisse") || it.plain.contains("keine Ergebnisse") } ?: bad("Keine Antwort: ${observed(admin, m)}")
        val lines = r.plain.lines().count { it.contains("schrieb") }
        check(Text.has(r.plain, "Suchergebnisse") && lines == 1) { "Erwartet genau 1 Treffer, erhalten: „${r.plain.trim().take(300)}“" }
        note("Genau ein Treffer für $uuid")
    }
    lookup("chat-90", "lookup --message", "--message google", { admin.chat("GooGle.com ${uniq()}"); sleep(1000) }) { t ->
        val rows = t.lines().filter { it.contains("schrieb") }
        when {
            rows.isEmpty() -> "keine Treffer"
            !rows.all { it.contains("google", ignoreCase = true) } -> "Treffer ohne „google“ (Inhaltsfilter wirkt nicht)"
            else -> null
        }
    }
    lookup("chat-91", "lookup ohne Ergebnis", "--message zzzqqqxx") { if (it.contains("keine Ergebnisse")) null else "Treffer statt „Es wurden keine Ergebnisse gefunden.“" }

    scenario("chat-92", "lookup Timeout") {
        requireDestructive()
        on(PAPER, admin)
        net.compose("stop", "player-microservice")
        try {
            val m = admin.run("surfplayer chat lookup")
            val r = admin.expect("zu lange gedauert", since = m, timeout = 20000)
            note("Bei gestopptem Microservice: „${r.plain.trim()}“")
        } finally {
            net.compose("start", "player-microservice")
            sleep(15000)
        }
    }

    scenario("chat-93", "/surfplayer chat reload") {
        on(PAPER, admin)
        val m = admin.run("surfplayer chat reload")
        val r = admin.expect("Die Konfiguration von chat wurde neu geladen", since = m)
        on(LOBBY, admin)
        val m2 = admin.run("surfplayer chat reload")
        val r2 = admin.expect("Die Konfiguration von chat wurde neu geladen", since = m2)
        note("Paper: „${r.plain.trim()}“; Minestom: „${r2.plain.trim()}“")
    }

    scenario("chat-94", "/surfplayer chat status") {
        on(PAPER, admin)
        val m = admin.run("surfplayer chat status")
        val r = admin.expect("Der Chat ist auf diesem Server", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-95", "/surfplayer chat toggle aus") {
        on(PAPER, admin)
        val m = admin.run("surfplayer chat toggle aus")
        try {
            val r = admin.expect("Der Chat wurde auf diesem Server deaktiviert", since = m)
            val notify = admin.messagesSince(m).firstOrNull { it.plain.contains("hat den Chat auf dem Server") }
            sleep(500)
            val db = net.sql("""SELECT server, chat_enabled FROM "surf-chat".chat_functionalities""")
            if (db.none { it[0] == "paper" && it[1] == "f" }) bad("chat_functionalities zeigt paper nicht deaktiviert: $db; „${r.plain.trim()}“")
            note("„${r.plain.trim()}“; Team-Info: „${notify?.plain?.trim() ?: "keine"}“; DB: $db")
            if (notify == null) odd("Keine Team-Benachrichtigung „${A} hat den Chat … deaktiviert“; ${notes.last()}")
        } finally {
            admin.cmd("surfplayer chat toggle an"); sleep(1000)
        }
    }

    scenario("chat-96", "/surfplayer chat toggle an") {
        on(PAPER, admin)
        admin.cmd("surfplayer chat toggle aus"); sleep(1000)
        val m = admin.run("surfplayer chat toggle an")
        val r = admin.expect("Der Chat wurde auf diesem Server aktiviert", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("chat-97", "/surfplayer chat servers") {
        on(PAPER, admin)
        val m = admin.run("surfplayer chat servers")
        val r = admin.await(8000, m) { it.plain.contains("✔") || it.plain.contains("✘") } ?: bad("Keine Serverliste: ${observed(admin, m)}")
        if (!r.plain.contains("Chat auf den Servern", true) && !r.plain.contains("ᴄʜᴀᴛ ᴀᴜғ ᴅᴇɴ sᴇʀᴠᴇʀɴ")) odd("Überschrift „Chat auf den Servern“ fehlt: „${r.plain.trim().take(200)}“")
        note("„${r.plain.trim().replace("\n", " ⏎ ").take(250)}“")
    }

    scenario("chat-98", "Functionality persistent") {
        requireDestructive()
        on(PAPER, admin)
        admin.cmd("surfplayer chat toggle aus"); sleep(1500)
        try {
            restartPaper()
            on(PAPER, p1)
            val m = p1.say(uniq())
            val r = p1.expect("deaktiviert", since = m)
            note("Nach Paper-Neustart weiterhin: „${r.plain.trim()}“")
        } finally {
            on(PAPER, admin)
            admin.cmd("surfplayer chat toggle an"); sleep(1000)
        }
    }

    for (id in listOf("chat-99", "chat-100", "chat-101", "chat-102", "chat-103", "chat-104", "chat-105")) {
        notAutomatable(id, "KI-Moderation braucht einen OpenAI-API-Key und Discord-Webhook; im Testnetz nicht konfiguriert.")
    }

    scenario("chat-106", "Chat-History-Logging") {
        on(PAPER, admin, p1)
        createClan(admin, "Histclan", "HST")
        try {
            admin.chat(uniq("g")); sleep(500)
            admin.cmd("msg ${P1} ${uniq("d")}"); sleep(500)
            admin.cmd("tc ${uniq("t")}"); sleep(500)
            admin.cmd("cc ${uniq("c")}"); sleep(2000)
            val types = net.sql("""SELECT DISTINCT type FROM "surf-chat".chat_histories WHERE sender_uuid = '${admin.uuid}' AND sent_at > (now() at time zone 'utc') - interval '3 minutes'""").map { it[0] }
            val missing = listOf("GLOBAL", "DIRECT", "TEAM", "CLAN_CHAT").filter { it !in types }
            check(missing.isEmpty()) { "Typen in chat_histories: $types, fehlend: $missing" }
            note("chat_histories enthält $types")
        } finally {
            resetClans()
        }
    }

    scenario("chat-107", "Permission-Gates Admin-Chatbefehle") {
        on(PAPER, p1)
        p1.expectCommandUnavailable("spy")
        p1.expectCommandUnavailable("slowchat")
        p1.expectCommandUnavailable("surfplayer")
        note("/spy, /slowchat, /surfplayer fehlen im Befehlsbaum von ${P1}")
    }
}

/** Scenarios that restart or stop containers run only with --destructive. */
fun dev.slne.surf.player.tests.dsl.Ctx.requireDestructive() {
    if (System.getProperty("playertests.destructive") != "true") {
        skip("Startet/stoppt Container; nur mit --destructive ausgeführt (würde menschliche Tester auf Port 25565 stören)")
    }
}
