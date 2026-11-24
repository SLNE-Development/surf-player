package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.bot.Received
import dev.slne.surf.player.tests.bot.Window
import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.Prefix
import dev.slne.surf.player.tests.dsl.notAutomatable
import dev.slne.surf.player.tests.dsl.scenario

/** Opens a menu with [command] and waits for the window and its items. */
fun Ctx.openMenu(bot: Bot, command: String, titleContains: String): Window {
    bot.closeWindow()
    bot.cmd(command)
    val w = eventually(6000, "Menü „$titleContains“ für ${bot.name}") { bot.window?.takeIf { it.titleText.contains(titleContains) && it.items.isNotEmpty() } }
    sleep(500)
    return w
}

fun Ctx.slotWith(w: Window, name: String): Int =
    w.nonEmpty().entries.firstOrNull { dev.slne.surf.player.tests.bot.Text.has(it.value.nameText, name) }?.key
        ?: bad("Kein Item „$name“ in „${w.titleText}“: ${w.nonEmpty().values.map { it.nameText }}")

private fun Ctx.settingValue(bot: Bot, key: String): String =
    net.sql("""SELECT value FROM "surf-settings".settings_entries WHERE player_uuid = '${bot.uuid}' AND setting_name = '$key'""")
        .firstOrNull()?.firstOrNull() ?: "(default)"

private fun Ctx.infoValue(bot: Bot, key: String): String {
    val m = bot.run("settings info $key")
    val r = bot.expect("Die Einstellung", since = m)
    return Regex("hat den Wert (\\w+)").find(r.plain)?.groupValues?.get(1) ?: r.plain.trim()
}

private val CATEGORIES = listOf(
    Triple("settings-02", "Chat", listOf("Nachrichten und Benachrichtigungen im Chat", "Verbindungsnachrichten", "Todesnachrichten", "Private Nachrichten", "Chat Pings")),
    Triple("settings-03", "Clan", listOf("Clan-Chat und Clan-Einladungen", "Clan-Chat", "Clan-Einladungen")),
    Triple("settings-04", "Freunde", listOf("Benachrichtigungen und Sounds deiner Freunde", "Freunde-Benachrichtigungen", "Freundschaftsanfragen annehmen", "Freundschaftsanfrage-Benachrichtigungen", "Freunde-Sounds")),
    Triple("settings-05", "Lobby", listOf("Einstellungen für die Lobby", "Scroll-Sound")),
    Triple("settings-06", "Allgemeines", listOf("Anzeige- und Verbindungseinstellungen", "Nametags anzeigen", "Automatische Rückkehr")),
)

fun registerSettings() {
    scenario("settings-01", "/settings öffnet Menü") {
        on(PAPER, p1)
        val w = openMenu(p1, "settings", "Einstellungen")
        val items = w.nonEmpty()
        val cats = listOf("Chat", "Clan", "Freunde", "Lobby", "Allgemeines").associateWith { c -> items.entries.firstOrNull { dev.slne.surf.player.tests.bot.Text.norm(it.value.nameText.trim()) == c.lowercase() }?.key }
        check(cats.values.none { it == null }) { "Kategorien fehlen: ${cats.filterValues { it == null }.keys}; Items: ${items.values.map { it.nameText }}" }
        note("Titel „${w.titleText}“ (${w.type}), Kategorien in Slots $cats")
        if (cats.values.toList() != listOf(11, 12, 13, 14, 15)) odd("Kategorien nicht in Reihe 2, Spalten 3–7: $cats")
        p1.closeWindow()
    }

    for ((id, cat, expected) in CATEGORIES) scenario(id, "Kategorie $cat (Item)") {
        on(PAPER, p1)
        val w = openMenu(p1, "settings", "Einstellungen")
        val item = w.item(slotWith(w, cat))!!
        val lore = item.loreText.joinToString(" | ")
        val missing = expected.filter { !dev.slne.surf.player.tests.bot.Text.has(lore, it) }
        p1.closeWindow()
        check(missing.isEmpty()) { "Lore von $cat ohne $missing: „$lore“" }
        note("$cat: „$lore“")
    }

    scenario("settings-07", "Kategorie öffnen") {
        on(PAPER, p1)
        val w = openMenu(p1, "settings", "Einstellungen")
        val m = p1.mark()
        p1.clickSlot(slotWith(w, "Chat"))
        val sub = eventually(5000, "Fenster „Einstellungen - Chat“") { p1.window?.takeIf { it.titleText.contains("Chat") && it.items.isNotEmpty() } }
        sleep(500)
        val sound = p1.since(m).firstOrNull { it.kind == Received.Kind.SOUND }
        val names = sub.nonEmpty().values.map { it.nameText }
        note("„${sub.titleText}“ mit $names; Klick-Sound: ${sound?.extra ?: "keiner"}")
        p1.closeWindow()
        if (sound == null) odd("Kein Klick-Sound; ${notes.last()}")
    }

    scenario("settings-08", "Setting umschalten im Menü") {
        on(PAPER, p1)
        setSetting(p1, "chat:pings", true)
        val w = openMenu(p1, "settings", "Einstellungen")
        p1.clickSlot(slotWith(w, "Chat"))
        val sub = eventually(5000, "Chat-Kategorie") { p1.window?.takeIf { it.titleText.contains("Chat") && it.items.isNotEmpty() } }
        sleep(500)
        val slot = slotWith(sub, "Chat Pings")
        val before = sub.item(slot)!!.loreText.joinToString("|")
        p1.clickSlot(slot)
        sleep(1200)
        val after = p1.window?.item(slot)?.loreText?.joinToString("|") ?: ""
        p1.closeWindow(); sleep(1500)
        setSetting(p1, "chat:pings", true)
        check(before != after) { "Lore unverändert nach Klick: „$before“" }
        note("Lore vorher „$before“, nachher „$after“")
    }

    scenario("settings-09", "Speichern erst beim Schließen") {
        on(PAPER, p1)
        setSetting(p1, "chat:pings", true)
        val w = openMenu(p1, "settings", "Einstellungen")
        p1.clickSlot(slotWith(w, "Chat"))
        val sub = eventually(5000, "Chat-Kategorie") { p1.window?.takeIf { it.titleText.contains("Chat") && it.items.isNotEmpty() } }
        sleep(500)
        p1.clickSlot(slotWith(sub, "Chat Pings")); sleep(1500)
        val whileOpen = settingValue(p1, "chat:pings")
        p1.closeWindow(); sleep(2000)
        val afterClose = settingValue(p1, "chat:pings")
        val info = infoValue(p1, "chat:pings")
        setSetting(p1, "chat:pings", true)
        check(afterClose == "false" && info == "false") { "Nach Schließen DB=$afterClose, info=$info" }
        note("DB während offen: $whileOpen, nach Schließen: $afterClose; /settings info: $info")
        if (whileOpen == "false") odd("Wert schon vor dem Schließen gespeichert; ${notes.last()}")
    }

    scenario("settings-10", "Hin-und-her ohne Änderung") {
        on(PAPER, p1)
        setSetting(p1, "chat:pings", true)
        val w = openMenu(p1, "settings", "Einstellungen")
        p1.clickSlot(slotWith(w, "Chat"))
        val sub = eventually(5000, "Chat-Kategorie") { p1.window?.takeIf { it.titleText.contains("Chat") && it.items.isNotEmpty() } }
        sleep(500)
        val slot = slotWith(sub, "Chat Pings")
        p1.clickSlot(slot); sleep(800); p1.clickSlot(slot); sleep(800)
        p1.closeWindow(); sleep(1500)
        val v = infoValue(p1, "chat:pings")
        check(v == "true") { "Wert nach zweimal Umschalten: $v" }
        note("Wert bleibt true")
    }

    scenario("settings-11", "Zurück-Button") {
        on(PAPER, p1)
        val w = openMenu(p1, "settings", "Einstellungen")
        p1.clickSlot(slotWith(w, "Chat"))
        val sub = eventually(5000, "Chat-Kategorie") { p1.window?.takeIf { it.titleText.contains("Chat") && it.items.isNotEmpty() } }
        sleep(500)
        p1.clickSlot(slotWith(sub, "Zurück"))
        val main = eventually(5000, "Hauptmenü nach Zurück") { p1.window?.takeIf { it.titleText.trim() == "Einstellungen" && it.items.isNotEmpty() } }
        note("Zurück führt zu „${main.titleText}“")
        p1.closeWindow()
    }

    scenario("settings-12", "Kategorie-Seiten") {
        on(PAPER, p1)
        val seen = mutableListOf<String>()
        for (cat in listOf("Clan", "Freunde", "Lobby", "Allgemeines")) {
            val w = openMenu(p1, "settings", "Einstellungen")
            p1.clickSlot(slotWith(w, cat))
            val sub = eventually(5000, "Kategorie $cat") { p1.window?.takeIf { it.titleText.contains(cat) && it.items.isNotEmpty() } }
            sleep(400)
            seen += "„${sub.titleText}“: ${sub.nonEmpty().values.map { it.nameText }.filter { it.isNotBlank() && it != "Zurück" }}"
            p1.closeWindow()
        }
        note(seen.joinToString("; "))
    }

    scenario("settings-13", "/settings toggle <Setting>") {
        on(PAPER, p1)
        setSetting(p1, "chat:pings", true)
        val m = p1.run("settings toggle chat:pings")
        val r = p1.expect("Die Einstellung chat:pings wurde auf", since = m, prefix = Prefix.Success)
        setSetting(p1, "chat:pings", true)
        note("„${r.plain.trim()}“")
    }

    scenario("settings-14", "/settings toggle an") {
        on(PAPER, p1)
        setSetting(p1, "chat:pings", false)
        val m = p1.run("settings toggle chat:pings an")
        val r = p1.expect("wurde auf", since = m)
        check(r.plain.contains("true") || r.plain.contains("an") || r.plain.contains("aktiviert")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("settings-15", "/settings toggle aus") {
        on(PAPER, p1)
        val m = p1.run("settings toggle chat:pings aus")
        val r = p1.expect("wurde auf", since = m)
        setSetting(p1, "chat:pings", true)
        check(r.plain.contains("false") || r.plain.contains("aus") || r.plain.contains("deaktiviert")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }

    scenario("settings-16", "/settings info") {
        on(PAPER, p1)
        val m = p1.run("settings info chat:pings")
        val r = p1.expect("Die Einstellung chat:pings hat den Wert", since = m, prefix = Prefix.Info)
        note("„${r.plain.trim()}“")
    }

    scenario("settings-17", "/settings unbekanntes Setting") {
        on(PAPER, p1)
        for (cmd in listOf("settings info foo:bar", "settings toggle foo:bar")) {
            val m = p1.run(cmd)
            val r = p1.firstMessage(m) ?: bad("/$cmd: keine Antwort")
            check(r.plain.contains("Die Einstellung wurde nicht gefunden.")) { "/$cmd: „${r.plain.trim()}“" }
            note("/$cmd: „${r.plain.trim()}“")
        }
    }

    scenario("settings-18", "/settings ungültiger Zustand") {
        on(PAPER, p1)
        val m = p1.run("settings toggle chat:pings vielleicht")
        val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("Bitte gib 'an' oder 'aus' an.")) { "„${r.plain.trim().replace("\n", " ")}“" }
        note("„${r.plain.trim().replace("\n", " ")}“")
    }

    scenario("settings-19", "Nicht-Boolean-Setting") {
        on(PAPER, admin)
        net.sql("""INSERT INTO "surf-settings".settings_settings (name, default_value) SELECT 'test:number', '5' WHERE NOT EXISTS (SELECT 1 FROM "surf-settings".settings_settings WHERE name = 'test:number')""")
        try {
            admin.cmd("surfplayer settings refresh"); sleep(2000)
            val m = admin.run("settings toggle test:number")
            val r = admin.firstMessage(m) ?: bad("Keine Antwort")
            check(r.plain.contains("Nur Boolean")) { "„${r.plain.trim()}“" }
            note("„${r.plain.trim()}“")
        } finally {
            net.sql("""DELETE FROM "surf-settings".settings_settings WHERE name = 'test:number'""")
            admin.cmd("surfplayer settings refresh"); sleep(1000)
        }
    }

    scenario("settings-20", "/surfplayer settings refresh") {
        on(PAPER, admin)
        val m = admin.run("surfplayer settings refresh")
        val r = admin.expect("Es wurden alle Einstellungen neu geladen", since = m, prefix = Prefix.Success)
        note("„${r.plain.trim()}“")
    }

    scenario("settings-21", "/surfplayer settings list") {
        on(PAPER, admin)
        val m = admin.run("surfplayer settings list")
        val r = admin.expect("Verfügbare Einstellungen", since = m)
        check(r.plain.contains("friend:requests") && !r.plain.contains("scoreboard:show-scoreboard")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim().take(300)}“")
    }

    scenario("settings-22", "/surfplayer settings ohne Permission") {
        on(PAPER, p1)
        p1.expectCommandUnavailable("surfplayer")
        note("/surfplayer fehlt im Befehlsbaum von ${P1}")
    }

    scenario("settings-23", "Setting persistiert & übergreifend") {
        on(PAPER, p1)
        setSetting(p1, "chat:pings", false)
        try {
            on(LOBBY, p1)
            val v = infoValue(p1, "chat:pings")
            check(v == "false") { "Lobby zeigt $v" }
            note("In der Lobby: chat:pings = $v")
        } finally {
            setSetting(p1, "chat:pings", true)
        }
    }

    scenario("settings-24", "chat:pings an → Ping bei Erwähnung") {
        on(PAPER, admin, p1)
        setSetting(p1, "chat:pings", true)
        val m = p1.mark()
        admin.chat("${P1} ${uniq()}")
        val s = p1.expectSound("chicken", since = m)
        note("Sound ${s.extra}")
    }

    scenario("settings-25", "chat:pings aus → kein Ping bei Erwähnung") {
        on(PAPER, admin, p1)
        setSetting(p1, "chat:pings", false)
        try {
            val t = uniq()
            val m = p1.mark()
            admin.chat("${P1} $t")
            val r = p1.expect(t, since = m)
            p1.expectNoSound("chicken", since = m, within = 2000)
            check(r.plain.contains("@${P1}")) { "Keine Hervorhebung: „${r.plain}“" }
            note("Hervorhebung „${r.plain.trim()}“, kein Sound")
        } finally {
            setSetting(p1, "chat:pings", true)
        }
    }

    scenario("settings-26", "chat:pings an → Ping bei PN") {
        on(PAPER, admin); on(LOBBY, p1)
        setSetting(p1, "chat:pings", true)
        val m = p1.mark()
        admin.cmd("msg ${P1} hi ${uniq()}")
        val s = p1.expectSound("", since = m)
        note("Sound ${s.extra} bei PN (anderer Server)")
    }

    scenario("settings-27", "chat:pings aus → kein Ping bei PN") {
        on(PAPER, admin, p1)
        setSetting(p1, "chat:pings", false)
        try {
            val t = uniq()
            val m = p1.mark()
            admin.cmd("msg ${P1} $t")
            p1.expect(t, since = m)
            sleep(1500)
            val sounds = p1.since(m).filter { it.kind == Received.Kind.SOUND }
            check(sounds.isEmpty()) { "Sound trotz chat:pings aus: ${sounds.map { it.extra }}" }
            note("PN ohne Sound")
        } finally {
            setSetting(p1, "chat:pings", true)
        }
    }

    scenario("settings-28", "chat:direct-messages an") {
        on(PAPER, admin, p1)
        setSetting(p1, "chat:direct-messages", true)
        val t = uniq(); val m = p1.mark()
        admin.cmd("msg ${P1} $t")
        p1.expect(t, since = m)
        note("PN kommt an")
    }

    scenario("settings-29", "chat:direct-messages aus") {
        on(PAPER, admin); on(LOBBY, p1)
        setSetting(p1, "chat:direct-messages", false)
        try {
            val t = uniq(); val m = p1.mark()
            val ma = admin.run("msg ${P1} $t")
            p1.expectNone(t, since = m, within = 2500)
            val out = admin.firstMessage(ma)
            note("${P1} erhält nichts; ${A} sieht: „${out?.plain?.trim() ?: "nichts"}“")
        } finally {
            setSetting(p1, "chat:direct-messages", true)
        }
    }

    scenario("settings-30", "chat:connection-messages an") {
        on(PAPER, admin); on(LOBBY, p1)
        setSetting(admin, "chat:connection-messages", true)
        val m = admin.mark()
        on(PAPER, p1)
        val r = admin.expect("[+]", "${P1}", since = m)
        note("„${r.plain.trim()}“")
    }

    scenario("settings-31", "chat:connection-messages aus") {
        on(PAPER, admin); on(LOBBY, p1)
        setSetting(admin, "chat:connection-messages", false)
        try {
            val m = admin.mark()
            on(PAPER, p1); on(LOBBY, p1)
            admin.expectNone("${P1}", since = m, within = 2000)
            note("Keine Join-/Quit-Nachricht von ${P1}")
        } finally {
            setSetting(admin, "chat:connection-messages", true)
        }
    }

    scenario("settings-32", "chat:death-messages an") {
        on(PAPER, admin, p1)
        setSetting(admin, "chat:death-messages", true)
        val m = admin.mark()
        net.console(PAPER, "kill ${P1}", 1500)
        val r = admin.await(4000, m) { it.kind == Received.Kind.SYSTEM && (it.plain.contains("${P1}") || it.plain.contains("death")) }
        note("Todesnachricht: „${r?.plain?.trim() ?: "keine"}“")
        if (r == null) bad("Keine Todesnachricht bei chat:death-messages an")
    }

    scenario("settings-33", "chat:death-messages aus") {
        on(PAPER, admin, p1)
        setSetting(admin, "chat:death-messages", false)
        try {
            val m = admin.mark()
            net.console(PAPER, "kill ${P1}", 1500)
            val r = admin.await(4000, m) { it.kind == Received.Kind.SYSTEM && (it.plain.contains("${P1}") || it.plain.contains("death")) }
            if (r != null) bad("Todesnachricht trotz chat:death-messages aus: „${r.plain.trim()}“")
            note("Keine Todesnachricht")
        } finally {
            setSetting(admin, "chat:death-messages", true)
        }
    }

    notAutomatable("settings-34", "lobby:scroll-sound wird vom externen Lobby-Plugin ausgewertet; Effekt nur clientseitig hörbar, kein Code in surf-player.")
    notAutomatable("settings-35", "lobby:scroll-sound (aus) hängt vom externen Lobby-Plugin ab, siehe settings-34.")

    scenario("settings-36", "clan:invites an") {
        on(PAPER, admin, p1); createClan(admin)
        setSetting(p1, "clan:invites", true)
        val m = p1.mark(); admin.cmd("clan invite ${P1}")
        p1.expect("eingeladen", since = m)
        note("Einladung kommt an"); resetClans()
    }

    scenario("settings-37", "clan:invites aus") {
        on(PAPER, admin, p1); createClan(admin)
        setSetting(p1, "clan:invites", false)
        try {
            val m = p1.mark()
            val ma = admin.run("clan invite ${P1}")
            p1.expectNone("eingeladen", since = m, within = 2500)
            note("Keine Einladung; ${A}: „${admin.firstMessage(ma)?.plain?.trim()}“")
        } finally {
            setSetting(p1, "clan:invites", true); resetClans()
        }
    }

    scenario("settings-38", "clan:chat-messages an") {
        on(PAPER, admin, p1); createClan(admin); joinClan(admin, p1)
        setSetting(p1, "clan:chat-messages", true)
        val t = uniq(); val m = p1.mark(); admin.cmd("cc $t")
        p1.expect(t, since = m)
        note("${P1} sieht Clanchat"); resetClans()
    }

    scenario("settings-39", "clan:chat-messages aus") {
        on(PAPER, admin, p1); createClan(admin); joinClan(admin, p1)
        setSetting(p1, "clan:chat-messages", false)
        try {
            val t = uniq(); val m = p1.mark(); admin.cmd("cc $t")
            p1.expectNone(t, since = m, within = 2500)
            val own = uniq(); val m2 = p1.mark(); p1.cmd("cc $own")
            p1.expect(own, since = m2)
            note("${P1} sieht DevAdmins Clanchat nicht, die eigene Clanchat-Nachricht schon")
        } finally {
            setSetting(p1, "clan:chat-messages", true); resetClans()
        }
    }

    scenario("settings-40", "friend:request-notifications an") {
        on(PAPER, admin, p1); resetFriends()
        setSetting(p1, "friend:request-notifications", true)
        val m = p1.mark(); admin.cmd("friend add ${P1}")
        val r = p1.expect("Freundschaftsanfrage von ${A}", since = m)
        check(r.plain.contains("✔") && r.plain.contains("❌")) { "„${r.plain}“" }
        note("„${r.plain.trim()}“"); resetFriends()
    }

    scenario("settings-41", "friend:request-notifications aus") {
        on(PAPER, admin, p1); resetFriends()
        setSetting(p1, "friend:request-notifications", false)
        try {
            val m = p1.mark(); admin.cmd("friend add ${P1}")
            p1.expectNone("Freundschaftsanfrage von ${A}", since = m, within = 2500)
            val sounds = p1.since(m).filter { it.kind == Received.Kind.SOUND }.map { it.extra }.filter { it.contains("pling") }
            val m2 = p1.run("friend requests")
            p1.expect("${A}", since = m2)
            check(sounds.isEmpty()) { "Pling trotz request-notifications aus" }
            note("Keine Nachricht, kein Sound, Anfrage in /friend requests vorhanden")
        } finally {
            setSetting(p1, "friend:request-notifications", true); resetFriends()
        }
    }

    scenario("settings-42", "friend:notifications an") {
        on(PAPER, p1); makeFriends(p1, admin)
        setSetting(p1, "friend:notifications", true)
        quit(admin)
        val m = p1.mark(); join(admin)
        val r = p1.expect("${A} ist nun online", since = m)
        note("„${r.plain.trim()}“ beim Netzwerk-Join"); resetFriends()
    }

    scenario("settings-43", "friend:notifications aus") {
        on(PAPER, p1); makeFriends(p1, admin)
        setSetting(p1, "friend:notifications", false)
        try {
            val m = p1.mark(); quit(admin); join(admin)
            p1.expectNone("${A} ist nun", since = m, within = 2500)
            val sounds = p1.since(m).filter { it.kind == Received.Kind.SOUND }.map { it.extra }.filter { it.contains("experience_orb") || it.contains("bass") }
            check(sounds.isEmpty()) { "Freundes-Sound trotz notifications aus: $sounds" }
            note("Keine Online-/Offline-Meldung, kein Sound")
        } finally {
            setSetting(p1, "friend:notifications", true); resetFriends()
        }
    }

    scenario("settings-44", "friend:sounds an") {
        on(PAPER, p1); resetFriends()
        setSetting(p1, "friend:sounds", true)
        val m = p1.mark()
        admin.cmd("friend add ${P1}"); p1.expect("Freundschaftsanfrage von ${A}", since = m)
        p1.cmd("friend accept ${A}"); sleep(1000)
        quit(admin); p1.expect("${A} ist nun offline", since = m)
        join(admin); p1.expect("${A} ist nun online", since = m); sleep(800)
        val sounds = p1.since(m).filter { it.kind == Received.Kind.SOUND }.map { it.extra }
        resetFriends()
        val missing = listOf("pling", "bass", "experience_orb").filter { s -> sounds.none { it.contains(s) } }
        note("Sounds: $sounds")
        if (missing.isNotEmpty()) bad("Fehlend $missing; ${notes.last()}")
    }

    scenario("settings-45", "friend:sounds aus") {
        on(PAPER, p1); makeFriends(p1, admin)
        setSetting(p1, "friend:sounds", false)
        try {
            val m = p1.mark(); quit(admin); join(admin)
            p1.expect("ist nun online", since = m); sleep(1000)
            val sounds = p1.since(m).filter { it.kind == Received.Kind.SOUND }.map { it.extra }.filter { it.contains("experience_orb") || it.contains("bass") }
            check(sounds.isEmpty()) { "Sound trotz friend:sounds aus: $sounds" }
            note("Meldung ohne Sound")
        } finally {
            setSetting(p1, "friend:sounds", true); resetFriends()
        }
    }

    scenario("settings-46", "nametag:show-nametags an") {
        on(PAPER, admin, p1)
        setSetting(p1, "nametag:show-nametags", true)
        sleep(1000)
        val t = p1.teamOf("${A}") ?: bad("Kein Team für ${A} bei ${P1}")
        check(t.visibility == "ALWAYS") { "Sichtbarkeit ${t.visibility}" }
        note("Team ${t.name}: nameTagVisibility=${t.visibility}")
    }

    scenario("settings-47", "nametag:show-nametags aus") {
        on(PAPER, admin, p1)
        setSetting(p1, "nametag:show-nametags", false)
        try {
            sleep(1500)
            val t = p1.teamOf("${A}") ?: bad("Kein Team für ${A}")
            val own = admin.teamOf(P1)?.visibility
            note("Mit show-nametags aus: ${P1} sieht ${A} mit nameTagVisibility=${t.visibility}; ${A} sieht ${P1} mit $own")
            if (t.visibility == "ALWAYS") bad("Fremde Nametags bleiben sichtbar trotz nametag:show-nametags aus; ${notes.last()}")
            if (own != "ALWAYS") bad("Eigener Nametag für andere versteckt; ${notes.last()}")
        } finally {
            setSetting(p1, "nametag:show-nametags", true)
        }
    }

    scenario("settings-52", "friend:requests an") {
        on(PAPER, admin, p1); resetFriends()
        setSetting(admin, "friend:requests", true)
        val m = p1.run("friend add ${A}")
        note("„${p1.expect("Freundschaftsanfrage an ${A} gesendet", since = m).plain.trim()}“"); resetFriends()
    }
    scenario("settings-53", "friend:requests aus") {
        on(PAPER, admin, p1); resetFriends()
        setSetting(admin, "friend:requests", false)
        try {
            val m = p1.run("friend add ${A}")
            note("„${p1.expect("${A} nimmt keine Freundschaftsanfragen an", since = m).plain.trim()}“")
        } finally { setSetting(admin, "friend:requests", true); resetFriends() }
    }
    scenario("settings-54", "/settings help") {
        on(PAPER, p1)
        note(helpContains(p1, "settings help", "/settings", "/settings toggle", "/settings info"))
    }
    scenario("settings-55", "/settings toggle Vorschläge") {
        on(PAPER, p1)
        val s = p1.suggestions("/settings toggle ")
        check(s.size >= 13 && "friend:requests" in s && "scoreboard:show-scoreboard" !in s) { "Vorschläge: $s" }
        note("${s.size} Vorschläge: $s")
    }
    scenario("settings-56", "Entfernte Einstellung scoreboard:show-scoreboard") {
        on(PAPER, p1)
        val m = p1.run("settings toggle scoreboard:show-scoreboard")
        note("„${p1.expect("Die Einstellung wurde nicht gefunden", since = m).plain.trim()}“")
    }
    scenario("settings-57", "/surfplayervelocity settings refresh/list") {
        on(LOBBY, admin)
        note(answerContains(admin, "surfplayervelocity settings refresh", "Es wurden alle Einstellungen neu geladen"))
        note(answerContains(admin, "surfplayervelocity settings list", "Verfügbare Einstellungen"))
    }
    scenario("settings-58", "/surfplayer settings Aktionsliste") {
        on(PAPER, admin)
        note(helpContains(admin, "surfplayer settings", "refresh", "list"))
    }
    notAutomatable("settings-59", "Reconnect-Setting wirkt nur für Server der Reconnect-Liste (z. B. survival); das Testnetz hat nur lobby/paper.")
    scenario("settings-60", "/settings toggle ohne Permission") {
        on(PAPER, p1)
        withPermission(p1, "surf.player.command.settings.toggle", false) {
            on(LOBBY, p1); on(PAPER, p1)
            val s = p1.suggestions("/settings ")
            check("toggle" !in s) { "toggle wird noch vorgeschlagen: $s" }
            val m = p1.run("settings info chat:pings")
            p1.expect("Die Einstellung chat:pings hat den Wert", since = m)
            note("Unterbefehle: $s; /settings info funktioniert")
        }
    }
    scenario("settings-61", "Präfixe der Einstellungs-Meldungen") {
        on(PAPER, p1)
        val m1 = p1.run("settings toggle chat:pings an"); val a = p1.expect("wurde auf", since = m1)
        val m2 = p1.run("settings info chat:pings"); val b = p1.expect("hat den Wert", since = m2)
        val m3 = p1.run("settings info foo:bar"); val c = p1.expect("nicht gefunden", since = m3)
        val issues = listOfNotNull(
            if (!hasPrefix(a, Prefix.Success)) "toggle ohne Erfolgs-Präfix" else null,
            if (!hasPrefix(b, Prefix.Info)) "info ohne Info-Präfix" else null,
            if (!hasPrefix(c, Prefix.Error)) "unbekannt ohne Fehler-Präfix" else null,
        )
        if (issues.isNotEmpty()) bad(issues.joinToString("; "))
        note("toggle Erfolg, info Info, unbekannt Fehler")
    }
    notAutomatable("settings-50", "Braucht einen Server „survival“ in der Reconnect-Liste; das Testnetz hat nur lobby und paper (siehe queue-23).")
    notAutomatable("settings-51", "Wie settings-50: kein Reconnect-Zielserver im Testnetz.")
}
