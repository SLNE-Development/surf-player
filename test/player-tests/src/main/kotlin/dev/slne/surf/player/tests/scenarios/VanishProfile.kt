package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Received
import dev.slne.surf.player.tests.dsl.Prefix
import dev.slne.surf.player.tests.dsl.notAutomatable
import dev.slne.surf.player.tests.dsl.scenario
import dev.slne.surf.player.tests.infra.Yaml

private const val NO_MOVEMENT = "Weltinteraktion/Bewegung: Bots können sich auf Paper 26.3 nicht frei bewegen (\"moved too quickly\"-Kick), keine Items/Blöcke/Mobs/Fahrzeuge benutzen."

fun registerVanish() {
    scenario("vanish-01", "/vanish aktivieren") {
        on(PAPER, admin, p1)
        vanish(admin, false)
        sleep(500)
        check(p1.sees(admin)) { "${P1} sieht ${A} schon vorher nicht" }
        val m = admin.run("vanish")
        try {
            val r = admin.expect("Du bist nun unsichtbar", since = m, prefix = Prefix.Success)
            val info = admin.expect("Mit /vanish spectate kannst du anderen Spielern zuschauen", since = m)
            eventually(4000, "${A} verschwindet für ${P1}") { if (!p1.sees(admin)) true else null }
            note("„${r.plain.trim().replace("\n", " ")}“ + „${info.plain.trim()}“; ${P1} sieht ${A} nicht mehr")
        } finally { vanish(admin, false) }
    }
    scenario("vanish-02", "Spoof-Quit für Nicht-Sehende") {
        on(PAPER, admin, p1); vanish(admin, false)
        val m = p1.mark(); admin.cmd("vanish")
        try { note("„${p1.expect("[-]", "${A}", since = m).plain.trim()}“") } finally { vanish(admin, false) }
    }
    scenario("vanish-03", "Info an Sehberechtigte") {
        on(PAPER, admin, p2); vanish(admin, false)
        withPermission(p2, "surf.player.vanish.bypass") {
            val m = p2.mark(); admin.cmd("vanish")
            try { note("${P2} (Bypass): „${p2.expect("${A}", "unsichtbar", since = m).plain.trim()}“") } finally { vanish(admin, false) }
        }
    }
    scenario("vanish-04", "Actionbar") {
        on(PAPER, admin); vanish(admin, true)
        try {
            val m = admin.mark(); sleep(3500)
            val bars = admin.since(m).filter { it.kind == Received.Kind.ACTIONBAR && it.plain.contains("unsichtbar") }
            check(bars.size >= 2) { "Nur ${bars.size} Actionbars in 3,5 s" }
            note("${bars.size}× „${bars.first().plain.trim()}“ in 3,5 s")
        } finally { vanish(admin, false) }
    }
    scenario("vanish-05", "/vanish deaktivieren") {
        on(PAPER, admin, p1); vanish(admin, true)
        val m = admin.mark(); val m1 = p1.mark()
        admin.cmd("vanish")
        val r = admin.expect("Du bist nun sichtbar", since = m)
        val spoof = p1.expect("[+]", "${A}", since = m1)
        eventually(4000, "${A} wieder sichtbar") { if (p1.sees(admin)) true else null }
        sleep(1500)
        val clear = admin.since(m).lastOrNull { it.kind == Received.Kind.ACTIONBAR }
        note("„${r.plain.trim()}“; ${P1}: „${spoof.plain.trim()}“; letzte Actionbar: „${clear?.plain?.trim() ?: "-"}“")
        if (clear != null && clear.plain.contains("unsichtbar")) odd("Actionbar nach Unvanish nicht geleert; ${notes.last()}")
    }
    scenario("vanish-06", "Spoof deaktiviert") {
        on(PAPER, admin, p1); vanish(admin, false)
        withConfig(PAPER, "vanish/config.yml", { Yaml.set(it, "spoof-connection-messages", "false") }, { reloadOn(PAPER, "vanish") }) {
            val m = p1.mark()
            vanish(admin, true); vanish(admin, false)
            p1.expectNone("${A}", since = m, within = 1500)
            note("Keine [+]/[-]-Nachrichten bei spoof-connection-messages: false")
        }
    }
    scenario("vanish-07", "/surfplayer vanish reload") {
        on(PAPER, admin)
        val m = admin.run("surfplayer vanish reload")
        note("„${admin.expect("Die Konfiguration von vanish wurde neu geladen", since = m).plain.trim()}“")
    }
    scenario("vanish-08", "/vanish ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("vanish"); note("/vanish fehlt bei ${P1}") }
    scenario("vanish-09", "Join im Vanish") {
        on(PAPER, p1, p2); vanish(admin, true)
        withPermission(p2, "surf.player.vanish.bypass") {
            try {
                on(LOBBY, admin)
                val m1 = p1.mark(); val m2 = p2.mark(); val ma = admin.mark()
                on(PAPER, admin)
                p1.expectNone("${A}", since = m1, within = 2500)
                val own = admin.await(4000, ma) { it.plain.contains("unsichtbar") }
                val seer = p2.expect("${A}", "unsichtbar betreten", since = m2)
                note("${P1} nichts; ${A}: „${own?.plain?.trim()}“; ${P2}: „${seer.plain.trim()}“")
            } finally { vanish(admin, false) }
        }
    }
    scenario("vanish-10", "Quit im Vanish") {
        on(PAPER, admin, p1, p2); vanish(admin, true)
        withPermission(p2, "surf.player.vanish.bypass") {
            try {
                val m1 = p1.mark(); val m2 = p2.mark()
                on(LOBBY, admin)
                p1.expectNone("${A}", since = m1, within = 2500)
                val seer = p2.expect("${A}", "unsichtbar verlassen", since = m2)
                note("${P1} nichts; ${P2}: „${seer.plain.trim()}“")
            } finally { vanish(admin, false) }
        }
    }
    scenario("vanish-11", "Vanish netzwerkweit") {
        on(PAPER, p1); vanish(admin, true)
        try {
            on(LOBBY, admin, p1); sleep(1500)
            val lobbySees = p1.sees(admin) || p1.tab[admin.uuid]?.listed == true
            on(PAPER, admin, p1)
            val still = isVanished(admin)
            check(still) { "Vanish nach Serverwechsel verloren" }
            note("Nach Lobby→Paper weiter vanished; in der Lobby für ${P1} ${if (lobbySees) "sichtbar (kein Vanish auf Minestom)" else "unsichtbar"}")
        } finally { vanish(admin, false) }
    }
    scenario("vanish-12", "Vanish-Priorität") {
        on(PAPER, admin, p1)
        withPermission(p1, "surf.player.vanish.priority.5") {
            vanish(admin, true)
            try { sleep(1500); note("${P1} (Priorität 5) sieht ${A} (*): ${p1.sees(admin)}") } finally { vanish(admin, false) }
        }
    }
    scenario("vanish-13", "Vanish-Bypass") {
        on(PAPER, admin, p1)
        withPermission(p1, "surf.player.vanish.bypass") {
            vanish(admin, true)
            try {
                sleep(1500)
                check(p1.sees(admin)) { "${P1} mit Bypass sieht ${A} nicht" }
                note("${P1} mit surf.player.vanish.bypass sieht ${A}")
            } finally { vanish(admin, false) }
        }
    }
    for (id in (14..25).map { "vanish-%02d".format(it) }) notAutomatable(id, NO_MOVEMENT)
    scenario("vanish-26", "Advancement-Nachricht unterdrückt") {
        on(PAPER, admin, p1); vanish(admin, true)
        try {
            net.console(PAPER, "advancement revoke ${A} only minecraft:story/root", 500)
            val m = p1.mark()
            net.console(PAPER, "advancement grant ${A} only minecraft:story/root", 1500)
            val r = p1.await(2500, m) { it.kind == Received.Kind.SYSTEM && it.plain.contains("${A}") }
            check(r == null) { "${P1} sieht „${r!!.plain.trim()}“" }
            note("Keine Advancement-Nachricht für den vanished ${A}")
        } finally { vanish(admin, false) }
    }
    scenario("vanish-27", "Tab-Vervollständigung") {
        on(PAPER, admin, p1); vanish(admin, true)
        try {
            val s = p1.suggestions("/msg Dev")
            check("${A}" !in s) { "${A} wird vorgeschlagen: $s" }
            note("Vorschläge für „/msg Dev“: $s")
        } finally { vanish(admin, false) }
    }
    notAutomatable("vanish-28", "Fly-State: braucht Fliegen im Creative und Rejoin mit Bewegung; Bots können sich nicht bewegen.")
    notAutomatable("vanish-29", "Fly-State bei Gamemode-Wechsel: braucht Fliegen (Bewegung).")
    scenario("vanish-30", "/vanish spectate an") {
        on(PAPER, admin); vanish(admin, true)
        try {
            val m = admin.run("vanish spectate")
            val r = admin.expect("Du bist nun im Zuschauermodus", since = m, prefix = Prefix.Success)
            sleep(800)
            val ctrl = admin.messagesSince(m).firstOrNull { it.plain.contains("Steuerung") }
            note("„${r.plain.trim()}“; Steuerung: „${ctrl?.plain?.trim()?.replace("\n", " ") ?: "fehlt"}“")
            if (ctrl == null) odd("Kein Steuerungskasten; ${notes.last()}")
        } finally { admin.cmd("vanish spectate"); sleep(800); vanish(admin, false) }
    }
    scenario("vanish-31", "Spectate: Weiter (F)") {
        on(PAPER, admin, p1); vanish(admin, true)
        admin.cmd("vanish spectate"); sleep(1000)
        try {
            val before = admin.teleports.get()
            val m = admin.mark(); admin.swapHands()
            val r = admin.expect("Du schaust nun", since = m)
            check(r.plain.contains(P1) || r.plain.contains(P2)) { "Ziel: „${r.plain.trim()}“" }
            check(admin.teleports.get() > before) { "Kein Teleport" }
            note("„${r.plain.trim()}“, teleportiert")
        } finally { admin.cmd("vanish spectate"); sleep(800); vanish(admin, false) }
    }
    scenario("vanish-32", "Spectate: Weiter ohne Spieler") {
        on(LOBBY, p1, p2); on(PAPER, admin); vanish(admin, true)
        admin.cmd("vanish spectate"); sleep(1000)
        try {
            val m = admin.mark(); admin.swapHands()
            note("„${admin.expect("kein weiterer Spieler", since = m).plain.trim()}“")
        } finally { admin.cmd("vanish spectate"); sleep(800); vanish(admin, false) }
    }
    scenario("vanish-33", "Spectate: Zurück (Sneak+F)") {
        on(PAPER, admin, p1, p2); vanish(admin, true)
        admin.cmd("vanish spectate"); sleep(1000)
        try {
            val m0 = admin.mark(); admin.sneak(true); sleep(200); admin.swapHands(); admin.sneak(false)
            val none = admin.firstMessage(m0, 3000)
            admin.swapHands(); sleep(800); admin.swapHands(); sleep(800)
            val m = admin.mark(); admin.sneak(true); sleep(200); admin.swapHands(); admin.sneak(false)
            val r = admin.expect("Du schaust nun wieder", since = m)
            note("Ohne Historie: „${none?.plain?.trim()}“; nach Weiter: „${r.plain.trim()}“")
        } finally { admin.cmd("vanish spectate"); sleep(800); vanish(admin, false) }
    }
    scenario("vanish-34", "Spectate: Teleport (2x Sneak)") {
        on(PAPER, admin, p1); vanish(admin, true)
        admin.cmd("vanish spectate"); sleep(1000)
        try {
            val m0 = admin.mark(); admin.sneak(true); admin.sneak(false); sleep(150); admin.sneak(true); admin.sneak(false)
            val none = admin.firstMessage(m0, 3000)
            admin.swapHands(); sleep(1000)
            val m = admin.mark(); admin.sneak(true); admin.sneak(false); sleep(150); admin.sneak(true); admin.sneak(false)
            val r = admin.expect("Du bist nun wieder bei", since = m)
            note("Ohne Ziel: „${none?.plain?.trim()}“; mit Ziel: „${r.plain.trim()}“")
        } finally { admin.cmd("vanish spectate"); sleep(800); vanish(admin, false) }
    }
    scenario("vanish-35", "Spectate: Ziel offline") {
        on(PAPER, admin, p1); vanish(admin, true)
        admin.cmd("vanish spectate"); sleep(1000)
        try {
            admin.swapHands(); sleep(1000)
            on(LOBBY, p1)
            val m = admin.mark(); admin.sneak(true); admin.sneak(false); sleep(150); admin.sneak(true); admin.sneak(false)
            note("„${admin.expect("nicht mehr online", since = m).plain.trim()}“")
        } finally { admin.cmd("vanish spectate"); sleep(800); vanish(admin, false) }
    }
    scenario("vanish-36", "/vanish spectate aus") {
        on(PAPER, admin); vanish(admin, true)
        admin.cmd("vanish spectate"); sleep(1000)
        try {
            val m = admin.run("vanish spectate")
            val r = admin.expect("Du schaust nun niemandem mehr zu", since = m)
            note("„${r.plain.trim()}“")
        } finally { vanish(admin, false) }
    }
    scenario("vanish-37", "Hotkeys ohne Vanish wirkungslos") {
        on(PAPER, admin, p1); vanish(admin, false)
        val m = admin.mark(); admin.swapHands(); sleep(1500)
        val r = admin.messagesSince(m).firstOrNull { it.plain.contains("schaust") }
        check(r == null) { "Spectate-Aktion ohne Vanish: „${r!!.plain}“" }
        note("F ohne Vanish löst keine Spectate-Aktion aus")
    }
    notAutomatable("vanish-38", "Minimap-Tracker: Bot müsste Xaero/JourneyMap-Pakete auswerten; Tracker-Kanäle siehe minimap-21..23.")
    scenario("vanish-47", "/vanish spectate ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("vanish"); note("/vanish fehlt bei DevPlayer1") }
    scenario("vanish-48", "/surfplayer vanish ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("surfplayer"); note("/surfplayer fehlt bei DevPlayer1") }
    scenario("vanish-49", "Nick-Feature entfernt") { on(PAPER, admin); admin.expectCommandUnavailable("nick"); note("/nick unbekannt") }
    scenario("vanish-50", "Vanish und Freundesliste") {
        on(PAPER, admin, p1); makeFriends(p1, admin); vanish(admin, true)
        try {
            val m = p1.run("friend list")
            val r = p1.expect(A, since = m)
            val line = r.plain.lines().first { it.contains(A) }
            check(line.contains("Offline")) { "„$line“" }
            note("„${line.trim()}“")
        } finally { vanish(admin, false); resetFriends() }
    }
}

fun registerProfile() {
    scenario("profile-01", "/profile öffnet Menü") {
        on(PAPER, p1)
        val w = openMenu(p1, "profile", "Profil")
        note("„${w.titleText}“ (${w.type}), Items: ${w.nonEmpty().map { "${it.key}=${it.value.nameText}" }}")
        val slot = w.nonEmpty().keys.first()
        p1.clickSlot(slot); sleep(800)
        val inv = p1.inventory.values.filter { it.id != 0 }
        p1.closeWindow()
        check(inv.none { it.nameText == w.item(slot)?.nameText && it.nameText.isNotBlank() }) { "Item ließ sich ins Inventar nehmen" }
    }
    scenario("profile-02", "Kopf-Item") {
        on(PAPER, p1)
        val w = openMenu(p1, "profile", "Profil")
        val head = w.nonEmpty().values.firstOrNull { it.nameText.contains("${P1}", true) || it.nameText.contains("ᴅᴇᴠ", true) } ?: w.nonEmpty().values.first()
        val lore = head.loreText.joinToString(" | ")
        p1.closeWindow()
        val missing = listOf("Rang", "Aktuelle Session", "Server", "Erster Login").filter { !dev.slne.surf.player.tests.bot.Text.has(lore, it) }
        check(missing.isEmpty()) { "Lore ohne $missing: „$lore“" }
        note("„${head.nameText}“: „$lore“")
    }
    scenario("profile-03", "Freunde-Item") {
        on(PAPER, p1)
        val w = openMenu(p1, "profile", "Profil")
        val slot = slotWith(w, "Freunde")
        val lore = w.item(slot)!!.loreText.joinToString(" | ")
        p1.clickSlot(slot); sleep(800)
        val still = p1.window?.titleText
        p1.closeWindow()
        check(lore.contains("nur über Befehle")) { "Lore „$lore“" }
        note("„$lore“; Klick: Fenster bleibt „$still“")
    }
    scenario("profile-04", "Einstellungen-Item") {
        on(PAPER, p1)
        val w = openMenu(p1, "profile", "Profil")
        val slot = slotWith(w, "Einstellungen")
        val lore = w.item(slot)!!.loreText.joinToString(" | ")
        val m = p1.mark()
        p1.clickSlot(slot)
        val settings = eventually(5000, "Einstellungsmenü nach Klick") { p1.window?.takeIf { it.titleText.contains("Einstellungen") && it.id != w.id } }
        val sound = p1.since(m).firstOrNull { it.kind == Received.Kind.SOUND }
        p1.closeWindow()
        check(lore.contains("Einstellungsmenü")) { "Lore „$lore“" }
        note("„$lore“; Klick öffnet „${settings.titleText}“, Sound ${sound?.extra ?: "keiner"}")
    }
    scenario("profile-06", "Permission-Gate /profile") {
        on(PAPER, p1)
        withPermission(p1, "surf.player.command.profile", false) {
            on(LOBBY, p1); on(PAPER, p1)
            p1.expectCommandUnavailable("profile")
            note("/profile fehlt bei surf.player.command.profile=false")
        }
    }
    scenario("profile-05", "Rang ohne Prefix") {
        on(PAPER, p1)
        val w = openMenu(p1, "profile", "Profil")
        val lore = w.nonEmpty().values.flatMap { it.loreText }.let { lines -> lines.indexOfFirst { dev.slne.surf.player.tests.bot.Text.has(it, "Rang") }.takeIf { i -> i >= 0 }?.let { i -> lines.drop(i).take(2).joinToString(" ") } } ?: bad("Keine Rang-Zeile")
        p1.closeWindow()
        check(lore.trim().endsWith("/")) { "„$lore“" }
        note("„$lore“")
    }
}
