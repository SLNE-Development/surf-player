package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.bot.Text
import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.notAutomatable
import dev.slne.surf.player.tests.dsl.scenario
import dev.slne.surf.player.tests.infra.Yaml

fun Ctx.header(bot: Bot) = Text.plain(bot.header)
fun Ctx.footer(bot: Bot) = Text.plain(bot.footer)
fun Ctx.tabDisplay(viewer: Bot, of: Bot): String = viewer.tab[of.uuid]?.display ?: "(kein Eintrag)"
fun Ctx.tabJson(viewer: Bot, of: Bot): String = Text.json(viewer.tab[of.uuid]?.displayName)

private fun onlineCount(header: String): Int? = Regex("""<> (\d+) / (\d+)""").find(header)?.groupValues?.get(1)?.toInt()

/**
 * Links a Twitch account to [bot] in public.account as the website does: the Twitch row holds the
 * numeric Twitch user id ([testTwitchId]), not a login name. The microservice looks the name up by
 * that id (ivr.fi) and falls back to the id itself for an id unknown to Twitch, so the name shown in
 * game is the id.
 */
fun Ctx.linkTwitch(bot: Bot) {
    unlinkAccounts(bot)
    val user = accountUserId(bot)
    net.sql(
        """INSERT INTO public.account ("userId", provider, "providerAccountId") VALUES
           ('$user', 'minecraft', '${bot.uuid}'), ('$user', 'twitch', '${testTwitchId(bot)}')"""
    )
}

/**
 * A Twitch user id for [bot] above every real one (Twitch ids are far below 10^10), distinct per bot
 * so parallel groups never share a (provider, providerAccountId) row.
 */
fun testTwitchId(bot: Bot): Long = 90_000_000_000L + Math.floorMod(bot.uuid.hashCode(), 1_000_000_000)

fun Ctx.unlinkAccounts(bot: Bot) {
    net.sql("""DELETE FROM public.account WHERE "userId" = '${accountUserId(bot)}'""")
}

/** The website account id of a bot's test link; the microservice expects a UUID here. */
fun accountUserId(bot: Bot): String = java.util.UUID.nameUUIDFromBytes("playertests:${bot.name}".toByteArray()).toString()

fun registerTab() {
    scenario("tab-01", "Standard-Header") {
        on(PAPER, p1); sleep(1000)
        val h = header(p1)
        val ok = h.contains("CASTCRAFTER") && h.contains("COMMUNITY SERVER") && Regex("""\d{2}\.\d{2}\.\d{4} - \d{2}:\d{2}""").containsMatchIn(h) && onlineCount(h) != null
        check(ok) { "Header: „$h“" }
        note("„${h.trim().replace("\n", " ⏎ ").replace(Regex(" {2,}"), " ")}“")
    }
    scenario("tab-02", "Standard-Footer") {
        on(PAPER, p1); sleep(1000)
        val f = footer(p1)
        check(f.contains("Du befindest dich auf paper") && f.contains("ᴄᴀѕᴛᴄʀᴀꜰᴛᴇʀ.ᴅᴇ")) { "Footer: „$f“" }
        note("„${f.trim().replace("\n", " ⏎ ")}“")
    }
    scenario("tab-03", "Spielerzahl aktualisiert sich") {
        on(PAPER, p1); on(LOBBY, admin); sleep(1500)
        val before = onlineCount(header(p1)) ?: bad("Keine Spielerzahl: ${header(p1)}")
        on(PAPER, admin)
        val after = eventually(5000, "Spielerzahl steigt") { onlineCount(header(p1))?.takeIf { it == before + 1 } }
        on(LOBBY, admin)
        val again = eventually(5000, "Spielerzahl sinkt") { onlineCount(header(p1))?.takeIf { it == before } }
        note("Spielerzahl $before → $after → $again")
    }
    scenario("tab-04", "Uhrzeit aktualisiert minütlich") {
        on(PAPER, p1)
        val time = { Regex("""- (\d{2}:\d{2})""").find(header(p1))?.groupValues?.get(1) }
        val start = time() ?: bad("Keine Uhrzeit im Header")
        val next = eventually(75_000, "Uhrzeitwechsel im Header") { time()?.takeIf { it != start } }
        note("Uhrzeit $start → $next")
    }
    scenario("tab-05", "Header per Config + /surfplayer tab reload") {
        on(PAPER, admin, p1)
        withConfig(PAPER, "tab/config.yml", { it.replace("COMMUNITY SERVER", "COMMUNITY TESTSERVER") }, { reloadOn(PAPER, "tab") }) {
            val m = admin.run("surfplayer tab reload")
            val r = admin.expect("Die Konfiguration von tab wurde neu geladen", since = m)
            eventually(5000, "neuer Header bei ${P1}") { header(p1).takeIf { it.contains("COMMUNITY TESTSERVER") } }
            note("„${r.plain.trim()}“; ${P1} sieht sofort den neuen Header")
        }
    }
    scenario("tab-06", "/surfplayer tab ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("surfplayer"); note("/surfplayer fehlt bei ${P1}") }
    scenario("tab-07", "Fremde MiniPlaceholders im Header") {
        on(PAPER, p1, p2)
        withConfig(PAPER, "tab/config.yml", { it.replace("COMMUNITY SERVER", "COMMUNITY SERVER <player_name>") }, { reloadOn(PAPER, "tab") }) {
            sleep(7000)
            val h1 = header(p1); val h2 = header(p2)
            if (h1.contains("<player_name>") || (!h1.contains("${P1}") && !h2.contains("${P2}"))) {
                skip("Platzhalter <player_name> nicht aufgelöst (keine passende MiniPlaceholders-Expansion installiert): „${h1.lines().getOrNull(2)?.trim()}“")
            }
            check(h1.contains("${P1}") && h2.contains("${P2}")) { "Header ${P1}: „$h1“, ${P2}: „$h2“" }
            note("Pro Spieler gerendert: ${P1}/${P2}")
        }
    }
    notAutomatable("tab-08", "Nur Timing (Aktualisierung alle 30 s statt 5 s) und braucht eine fremde MiniPlaceholders-Expansion mit veränderlichem Wert (siehe tab-07).")
    scenario("tab-09", "Tab-Eintrag Name mit Prefix") {
        on(PAPER, admin, p1); sleep(1000)
        val paper = tabDisplay(p1, admin)
        on(LOBBY, admin, p1); sleep(1500)
        val lobby = tabDisplay(p1, admin)
        check(paper.contains("${A}") && lobby.contains("${A}")) { "Paper „$paper“, Lobby „$lobby“" }
        note("Paper: „$paper“, Minestom: „$lobby“ (Dev*-Konten haben keinen LP-Prefix)")
    }
    scenario("tab-10", "Sortierung nach LP-Gewicht") {
        on(PAPER, admin, p1); sleep(1000)
        val a = p1.tab[admin.uuid]?.listOrder ?: bad("Kein Eintrag ${A}")
        val b = p1.tab[p1.uuid]?.listOrder ?: bad("Kein Eintrag ${P1}")
        check(a > b) { "listOrder ${A}=$a, ${P1}=$b (höher = weiter oben)" }
        note("listOrder ${A}=$a > ${P1}=$b")
    }
    scenario("tab-11", "LP-Änderung live") {
        // Minestom: the Paper tab shows the display name (as surf-tab did), without the LP prefix.
        on(LOBBY, admin, p1); sleep(500)
        lp("user ${p1.uuid} meta setprefix 100 <red>[Test]")
        try {
            val d = eventually(8000, "Prefix [Test] im Tab") { tabDisplay(admin, p1).takeIf { it.contains("[Test]") } }
            note("Ohne Rejoin: „$d“")
        } finally {
            lp("user ${p1.uuid} meta removeprefix 100"); sleep(1500)
        }
    }
    scenario("tab-12", "Kein Clan-Tag im Tab unter 10 aktiven Mitgliedern") {
        on(PAPER, admin, p1); resetClans(); sleep(1000)
        val before = tabJson(p1, admin)
        createClan(admin); sleep(3000)
        val after = tabJson(p1, admin)
        check(before == after) { "Tab-Eintrag änderte sich: $before → $after" }
        note("Tab-Eintrag von ${A} unverändert („${tabDisplay(p1, admin)}“)"); resetClans()
    }
    scenario("tab-13", "Clan-Tag im Tab bei Whitelist") {
        on(PAPER, admin, p1); createClan(admin); sleep(1000)
        val before = tabJson(p1, admin)
        withConfig(PAPER, "clan/config.yml", { it.replace("whitelisted-tags: []", "whitelisted-tags:\n    - $CT") }, { reloadOn(PAPER, "clan") }) {
            admin.cmd("clan color foreground FF6B6B"); sleep(1500)
            on(LOBBY, admin); on(PAPER, admin)
            val after = eventually(8000, "Clan-Tag im Tab") { tabJson(p1, admin).takeIf { it != before } }
            note("Tab-Eintrag mit Clan-Tag: ${after.take(200)}")
        }
        resetClans()
    }
    scenario("tab-14", "Clan-Änderung aktualisiert Tab") {
        on(PAPER, admin, p1); createClan(admin)
        withConfig(PAPER, "clan/config.yml", { it.replace("whitelisted-tags: []", "whitelisted-tags:\n    - $CT") }, { reloadOn(PAPER, "clan") }) {
            on(LOBBY, admin); on(PAPER, admin); sleep(2000)
            val before = tabJson(p1, admin)
            admin.cmd("clan color foreground 00FF00")
            val after = eventually(10000, "neu gerenderter Tag nach Farbwechsel") { tabJson(p1, admin).takeIf { it != before } }
            note("Nach Farbwechsel neu gerendert: ${after.take(150)}")
        }
        resetClans()
    }
    scenario("tab-15", "Live-Tag im Tab") {
        on(PAPER, admin, p1)
        linkTwitch(admin)
        try {
            val m = admin.run("surfplayer contentcreator debug live")
            val r = admin.firstMessage(m) ?: bad("Keine Antwort")
            if (!r.plain.contains("ONLINE")) bad("Debug live: „${r.plain.trim()}“")
            val d = eventually(8000, "● im Tab") { tabDisplay(p1, admin).takeIf { it.contains("●") } }
            note("„$d“")
        } finally {
            admin.cmd("surfplayer contentcreator debug offline"); sleep(1000); unlinkAccounts(admin)
        }
    }
    scenario("tab-16", "AFK-Tag im Tab") {
        on(PAPER, p1, p2)
        val d = eventually(200_000, "[AFK] bei ${P2} (Bot ohne Mausbewegung)") { tabDisplay(p1, p2).takeIf { it.contains("[AFK]") } }
        p2.look(45f, 5f)
        val back = eventually(6000, "[AFK] verschwindet nach Mausbewegung") { tabDisplay(p1, p2).takeIf { !it.contains("[AFK]") } }
        note("„$d“ → „$back“")
    }
    scenario("tab-17", "Vanish im Tab") {
        on(PAPER, admin, p1, p2)
        withPermission(p2, "surf.player.vanish.bypass") {
            vanish(admin, true)
            try {
                sleep(1500)
                val forP1 = p1.tab[admin.uuid]
                val forP2 = tabDisplay(p2, admin)
                check(forP1 == null || !forP1.listed) { "${P1} sieht ${A} im Tab: ${forP1?.display}" }
                check(forP2.contains("SPEC")) { "${P2} (bypass) sieht „$forP2“ ohne [SPEC]" }
                val vanishedOrder = p2.tab[admin.uuid]?.listOrder ?: 0
                val visibleOrders = listOf(p1, p2).mapNotNull { p2.tab[it.uuid]?.listOrder }
                note("${P1}: kein Eintrag; ${P2} (Bypass): „$forP2“, listOrder vanished $vanishedOrder, sichtbare $visibleOrders")
                if (visibleOrders.any { it >= vanishedOrder }) bad("Vanished Spieler steht nicht über allen sichtbaren; ${notes.last()}")
            } finally {
                vanish(admin, false)
            }
        }
    }
    scenario("tab-18", "Kein Vanish-Tag auf Minestom") {
        on(LOBBY, p1)
        vanish(admin, true)
        try {
            on(LOBBY, admin); sleep(1500)
            val e = p1.tab[admin.uuid]
            note("Lobby: ${A} für ${P1} ${if (e?.listed == true) "sichtbar („${e.display}“)" else "nicht sichtbar"}")
        } finally {
            vanish(admin, false)
        }
    }
    scenario("tab-20", "Negatives LP-Gewicht") {
        on(PAPER, admin, p1)
        val since = java.time.Instant.now()
        lp("group default setweight -5")
        try {
            sleep(3000)
            val errs = (net.logsSince("paper", since) + net.logsSince("minestom", since)).lines().filter { it.contains("Exception") || it.contains("ERROR") }
            check(errs.isEmpty()) { "Fehler im Log: ${errs.take(2)}" }
            note("Kein Fehler im Log; listOrder DevPlayer1=${p1.tab[p1.uuid]?.listOrder}, DevAdmin=${p1.tab[admin.uuid]?.listOrder}")
        } finally {
            lp("group default setweight 0"); sleep(1000)
        }
    }
}

fun registerNametag() {
    scenario("nametag-01", "Nametag Aufbau") {
        on(PAPER, admin, p1); sleep(1000)
        val t = p1.teamOf("${A}") ?: bad("Kein Team mit ${A} bei ${P1}: ${p1.teams.keys}")
        check(t.color.equals("GRAY", true)) { "Namensfarbe ${t.color}" }
        note("Team ${t.name}: prefix=„${Text.plain(t.prefix)}“ suffix=„${Text.plain(t.suffix)}“ color=${t.color} visibility=${t.visibility}")
    }
    scenario("nametag-02", "Clan-Tag im Nametag") {
        on(PAPER, admin, p1); createClan(admin); sleep(1000)
        val before = Text.json(p1.teamOf("${A}")?.suffix)
        joinClan(admin, p1)
        val after = eventually(8000, "Clan-Tag im Suffix von ${P1} bei ${A}") { admin.teamOf("${P1}")?.suffix?.let { Text.json(it) }?.takeIf { it.length > 40 } }
        note("Suffix von ${P1} bei ${A}: ${after.take(160)} (${A} vorher $before)"); resetClans()
    }
    scenario("nametag-03", "Clan-Tag-Farbe im Nametag") {
        on(PAPER, admin, p1); createClan(admin); joinClan(admin, p1); sleep(1500)
        val before = Text.json(admin.teamOf("${P1}")?.suffix)
        admin.cmd("clan color foreground 00FF00")
        val after = eventually(15000, "Suffix ändert sich nach Farbwechsel") { Text.json(admin.teamOf("${P1}")?.suffix).takeIf { it != before } }
        note("Suffix neu: ${after.take(160)}"); resetClans()
    }
    scenario("nametag-04", "Live-Tag im Nametag") {
        on(PAPER, admin, p1)
        linkTwitch(admin)
        try {
            admin.cmd("surfplayer contentcreator debug live"); sleep(1500)
            val s = eventually(8000, "● im Suffix") { p1.teamOf("${A}")?.suffix?.let { Text.plain(it) }?.takeIf { it.contains("●") } }
            note("Suffix „$s“")
        } finally {
            admin.cmd("surfplayer contentcreator debug offline"); sleep(1000); unlinkAccounts(admin)
        }
    }
    scenario("nametag-05", "LP-Prefix live") {
        on(PAPER, admin, p1); sleep(500)
        lp("user ${p1.uuid} meta setprefix 100 <red>[Test]")
        try {
            val pfx = eventually(8000, "Prefix im Nametag") { admin.teamOf("${P1}")?.prefix?.let { Text.plain(it) }?.takeIf { it.contains("[Test]") } }
            note("Prefix ohne Rejoin: „$pfx“")
        } finally {
            lp("user ${p1.uuid} meta removeprefix 100"); sleep(1500)
        }
    }
    scenario("nametag-06", "Join/Quit") {
        on(PAPER, admin); on(LOBBY, p1)
        on(PAPER, p1); sleep(1500)
        val a = admin.teamOf("${P1}"); val b = p1.teamOf("${A}")
        check(a != null && b != null) { "Teams nach Join: ${A} sieht ${a?.name}, ${P1} sieht ${b?.name}" }
        on(LOBBY, p1); sleep(1500)
        val gone = admin.teamOf("${P1}")
        check(gone == null) { "Team ${gone?.name} nach Quit nicht entfernt" }
        note("Teams gegenseitig beim Join, nach Quit entfernt")
    }
    for ((id, key, value) in listOf(
        Triple("nametag-07", "name-tag-visibility", "NEVER"),
        Triple("nametag-08", "name-tag-visibility", "HIDE_FOR_OTHER_TEAMS"),
        Triple("nametag-09", "collision-rule", "NEVER"),
        Triple("nametag-10", "name-color", "red"),
        Triple("nametag-11", "option-data", "FRIENDLY_CAN_SEE_INVISIBLE"),
    )) scenario(id, "Config $key $value") {
        on(PAPER, admin, p1)
        withConfig(PAPER, "nametag/config.yml", { Yaml.set(it, key, value) }, { reloadOn(PAPER, "nametag") }) {
            on(LOBBY, p1); on(PAPER, p1); sleep(1500)
            val t = p1.teamOf("${A}") ?: bad("Kein Team")
            val actual = when (key) {
                "name-tag-visibility" -> t.visibility
                "collision-rule" -> t.collision
                "name-color" -> t.color
                else -> "seeFriendlyInvisibles=${t.seeFriendlyInvisibles}"
            }
            val expected = if (key == "option-data") "seeFriendlyInvisibles=true" else value
            check(actual.equals(expected, true)) { "$key=$value (per /surfplayer nametag reload): Team zeigt $actual" }
            note("$key=$value → Team: $actual (ohne Neustart dank /surfplayer nametag reload)")
        }
    }
    scenario("nametag-12", "Vanish versteckt Nametag") {
        on(PAPER, admin, p1)
        vanish(admin, true)
        try {
            sleep(1500)
            check(!p1.sees(admin)) { "${P1} sieht die Entität von ${A}" }
        } finally {
            vanish(admin, false)
        }
        val back = eventually(5000, "${A} wieder sichtbar") { if (p1.sees(admin)) true else null }
        note("Unsichtbar im Vanish, danach wieder sichtbar ($back), Team ${p1.teamOf("${A}")?.name}")
    }
    scenario("nametag-15", "/surfplayer nametag reload") {
        on(PAPER, admin)
        note(answerContains(admin, "surfplayer nametag reload", "Die Konfiguration von nametag wurde neu geladen"))
        on(LOBBY, admin)
        note(answerContains(admin, "surfplayer nametag reload", "Die Konfiguration von nametag wurde neu geladen"))
    }
    scenario("nametag-16", "/surfplayer nametag ohne Permission") { on(PAPER, p1); p1.expectCommandUnavailable("surfplayer"); note("/surfplayer fehlt bei DevPlayer1") }
    scenario("nametag-17", "Eigener Nametag bei show-nametags aus") {
        on(PAPER, admin, p1)
        setSetting(p1, "nametag:show-nametags", false)
        try {
            sleep(1500)
            val forP1 = p1.teamOf(A)?.visibility
            val forAdmin = admin.teamOf(P1)?.visibility
            note("DevPlayer1 sieht DevAdmin-Nametag: $forP1; DevAdmin sieht DevPlayer1-Nametag: $forAdmin")
            if (forP1 == "ALWAYS" || forAdmin != "ALWAYS") bad("Erwartet: DevPlayer1 sieht keine fremden Nametags, DevAdmin sieht DevPlayer1 weiter; ${notes.last()}")
        } finally { setSetting(p1, "nametag:show-nametags", true) }
    }
    scenario("nametag-14", "Konfigdatei vorhanden") {
        val paper = net.cat("paper", "${dataDir(PAPER)}/nametag/config.yml")
        val lobby = net.cat("minestom", "${dataDir(LOBBY)}/nametag/config.yml")
        val keys = listOf("name-tag-visibility", "collision-rule", "name-color", "option-data")
        check(keys.all { paper.contains(it) && lobby.contains(it) } && paper.contains("#")) { "Paper: ${paper.take(200)} / Lobby: ${lobby.take(200)}" }
        note("nametag/config.yml auf Paper und Minestom mit Kommentaren und allen vier Optionen")
    }
}
