package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.scenario

private fun Ctx.errorLines(service: String, filter: (String) -> Boolean = { true }): List<String> =
    net.logsOfCurrentRun(service).lines().filter { l ->
        (l.contains("Exception") || l.contains("SEVERE") || l.contains(" ERROR") || l.contains("ERROR]")) && l.contains("surf.player") && filter(l)
    }

private fun Ctx.tables(schema: String) =
    net.sql("""SELECT table_name FROM information_schema.tables WHERE table_schema = '$schema'""").map { it[0] }.toSet()

private fun schema(id: String, schema: String, vararg expected: String) = scenario(id, "Schema $schema") {
    val t = tables(schema)
    val missing = expected.filter { it !in t }
    check(missing.isEmpty()) { "Schema $schema: fehlend $missing, vorhanden $t" }
    note("\"$schema\": ${t.sorted()}")
}

fun registerInfra() {
    scenario("infra-01", "Paper-Plugin startet") {
        val log = net.logsOfCurrentRun("paper")
        check(log.contains("surf-player-paper", true) || log.contains("SurfPlayer", true)) { "Kein surf-player-paper im Log" }
        val errs = errorLines("paper")
        check(errs.isEmpty()) { "Fehlerzeilen: ${errs.take(3)}" }
        on(PAPER, admin)
        note(helpContains(admin, "surfplayer", "settings", "playtime", "vanish", "chat", "clan", "contentcreator", "nametag", "tab", "minimap", "queue"))
        note("surf-player-paper geladen, keine Exception/SEVERE mit surf.player seit dem Start")
    }
    scenario("infra-02", "Velocity-Plugin startet") {
        val errs = errorLines("velocity")
        check(net.logsOfCurrentRun("velocity").contains("surf-player", true)) { "Kein surf-player im Velocity-Log" }
        check(errs.isEmpty()) { "Fehlerzeilen: ${errs.take(3)}" }
        on(LOBBY, admin)
        note(helpContains(admin, "surfplayervelocity", "settings", "clan", "connection", "queue"))
        note("surf-player-velocity geladen, keine Exception mit surf.player")
    }
    scenario("infra-03", "Minestom-Plugin startet") {
        val errs = errorLines("minestom")
        check(errs.isEmpty()) { "Fehlerzeilen: ${errs.take(3)}" }
        on(LOBBY, admin)
        val list = helpContains(admin, "surfplayer", "settings", "playtime", "chat", "clan", "contentcreator", "nametag", "tab", "minimap", "queue")
        if (list.contains("vanish")) bad("Minestom listet vanish: $list")
        note(list)
        note("Keine Exception mit surf.player im Minestom-Log seit dem Start")
    }
    scenario("infra-04", "Player-Microservice startet") {
        val log = net.logsOfCurrentRun("player-microservice")
        val traces = log.lines().filter { it.trimStart().startsWith("at ") || it.contains("Exception") }
        val causes = log.lines().filter { it.contains("Caused by") }.map { it.trim().take(200) }.distinct()
        check(traces.isEmpty()) { "Stacktrace seit Start: ${causes.ifEmpty { traces.take(2) }}" }
        check(log.contains("Microservice started successfully")) { "Kein „Microservice started successfully“ im Log" }
        note("„Microservice started successfully“, kein Stacktrace seit dem Start")
    }
    schema("infra-05", "surf-chat", "chat_functionalities", "chat_histories", "chat_ignorelist_entries", "chat_moderations")
    schema("infra-06", "surf-clan", "clan_clans", "clan_members", "clan_players", "clan_invites")
    schema("infra-07", "surf-friends", "friend_requests", "friendships", "friend_request_cooldowns")
    schema("infra-08", "surf-settings", "settings_settings", "settings_entries")
    schema("infra-09", "surf-playtime", "playtime_sessions", "playtime_streaks", "playtime_streak_pauses")
    schema("infra-10", "surf-shrieker", "shrieker_reports", "shrieker_data", "shrieker_bans")
    scenario("infra-11", "Tabelle public.account") {
        check("account" in tables("public")) { "public.account fehlt" }
        note("public.account vorhanden")
    }
    scenario("infra-12", "Standard-Settings registriert") {
        val rows = net.sql("""SELECT name, default_value FROM "surf-settings".settings_settings ORDER BY name""")
        val names = rows.map { it[0] }
        note("${rows.size} Einstellungen: ${rows.joinToString { "${it[0]}(${it[1]})" }}")
        if ("friend:requests" !in names) bad("friend:requests fehlt; ${notes.last()}")
        if ("scoreboard:show-scoreboard" in names) odd("scoreboard:show-scoreboard noch in settings_settings (alte Zeile, im Code entfernt); ${notes.last()}")
    }
    scenario("infra-13", "Schema-Override per Umgebungsvariable validiert") { requireDestructive(); skip("Braucht Microservice-Neustart mit geänderter Umgebung") }
    scenario("infra-14", "Friends-Cooldown-Env ungültig") { requireDestructive(); skip("Braucht Microservice-Neustart mit geänderter Umgebung") }
    scenario("infra-15", "RabbitMQ-Verbindungen surf-player") {
        val conns = net.rabbit("connections")
        val names = Regex("\"connection_name\":\"([^\"]*)\"").findAll(conns).map { it.groupValues[1] }.toList()
        val consumers = names.count { it == "surf-player/consumer" }
        val publishers = names.count { it == "surf-player/publisher" }
        val queues = Regex("\"name\":\"(surf-player[^\"]*)\"").findAll(net.rabbit("queues")).map { it.groupValues[1] }.toList()
        check(consumers >= 5 && publishers >= 5) { "surf-player/consumer: $consumers, /publisher: $publishers (erwartet je 5: Paper, Minestom, Velocity, velocity-bots, Microservice)" }
        check("surf-player" in queues) { "Queue surf-player fehlt: $queues" }
        note("$consumers× surf-player/consumer, $publishers× surf-player/publisher; Queues: ${queues.map { it.replace(Regex("[0-9a-f-]{36}"), "<uuid>") }.distinct()}")
    }
    scenario("infra-16", "Keine Alt-Queues/-Verbindungen") {
        val all = net.rabbit("queues") + net.rabbit("connections")
        val old = listOf("surf-chat", "surf-clan", "surf-friends", "surf-settings", "surf-playtime", "surf-shrieker").filter { all.contains("\"$it") }
        check(old.isEmpty()) { "Alte Namen gefunden: $old" }
        note("Keine Queues/Verbindungen alter Einzel-Plugins")
    }
    scenario("infra-17", "Redis-Schlüssel") {
        on(PAPER, admin, p1)
        vanish(admin, true); admin.cmd("msg ${P1} hi"); sleep(1000)
        val keys = net.redis("--scan", "--count", "1000").lines().filter { it.isNotBlank() }
        vanish(admin, false)
        val player = keys.filter { it.contains("surf-player:") }
        note("${keys.size} Schlüssel, surf-player: ${player.map { it.replace(Regex("[0-9a-f-]{36}"), "<uuid>") }.distinct().take(15)}")
        if (keys.any { it.startsWith("surf-vanish:") || it.startsWith("surf-chat:") || it.startsWith("surf-clan:") }) bad("Alte Schlüsselnamen vorhanden; ${notes.last()}")
        // surf-redis keeps sync structures under its own prefix, with ':' of the id replaced by '_'
        // (docs/protocol.md): the sync map surf-player:vanish:vanished-players is stored here.
        val vanishKeys = keys.filter { it.startsWith("surf-redis:sync:map:surf-player_vanish_vanished-players:") }
        if (vanishKeys.isEmpty()) bad("Kein Schlüssel surf-redis:sync:map:surf-player_vanish_vanished-players:*; ${notes.last()}")
        note("Vanish-Sync-Map: ${vanishKeys.sorted()}")
    }
    scenario("infra-18", "Paper-Datenordner pro Feature") {
        val ls = net.ls("paper", dataDir(PAPER))
        val files = listOf("chat/config.yml", "chat/ai-moderation.yml", "vanish/config.yml", "clan/config.yml", "playtime/config.yml", "nametag/config.yml", "tab/config.yml", "minimap/config.yml", "minimap/xaero.yml", "minimap/journeymap.yml", "minimap/voxelmap.yml", "queue/config.yml", "contentcreator/config.yml")
        val missing = files.filter { f -> !ls.contains(f.substringBefore('/') + ":") || !ls.contains(f.substringAfter('/')) }
        val dirs = Regex("""/([a-z]+):""").findAll(ls).map { it.groupValues[1] }.toList()
        val extra = dirs.filter { it in listOf("settings", "friends", "profile", "shrieker", "nick") }
        check(missing.isEmpty()) { "Fehlend: $missing" }
        check(extra.isEmpty()) { "Unerwartete Ordner: $extra" }
        note("Alle 13 Config-Dateien vorhanden; Ordner: $dirs")
    }
    scenario("infra-19", "Minestom-Datenordner pro Feature") {
        val ls = net.ls("minestom", dataDir(LOBBY))
        val dirs = Regex("""/([a-z]+):""").findAll(ls).map { it.groupValues[1] }.toList()
        check("vanish" !in dirs && "shrieker" !in dirs) { "vanish/shrieker-Ordner auf Minestom: $dirs" }
        note("Ordner: $dirs")
    }
    scenario("infra-20", "Velocity-Datenordner") {
        val ls = net.ls("velocity", dataDir("velocity"))
        val dirs = Regex("""/([a-z]+):""").findAll(ls).map { it.groupValues[1] }.toList()
        note("Ordner: $dirs; connection/config.yml ${ls.contains("config.yml")}, queue/velocity.yml ${ls.contains("velocity.yml")}")
    }
    scenario("infra-21", "Keine alten Plugin-Ordner/JARs") {
        val old = listOf("surf-chat", "surf-clan", "surf-friends", "surf-vanish", "surf-tab", "surf-nametag", "surf-settings", "surf-queue", "surf-playtime")
        val found = mutableListOf<String>()
        for ((svc, dir) in listOf("paper" to "/data/plugins", "velocity" to "/server/plugins", "minestom" to "/server/plugins")) {
            val ls = net.compose("exec", "-T", svc, "ls", dir).out.lines().map { it.trim() }
            found += ls.filter { f -> old.any { f.startsWith(it) } }.map { "$svc:$it" }
        }
        check(found.isEmpty()) { "Alte Plugins: $found" }
        note("Keine alten Einzel-Plugins in paper/velocity/minestom")
    }
    scenario("infra-22", "Microservice-Config social.properties") {
        val props = net.cat("player-microservice", "/app/config/social.properties")
        val log = net.logs("player-microservice")
        note("social.properties: „${props.lines().firstOrNull { it.contains("token") }?.trim()}“; Log „Discord bot token is not configured!“: ${log.contains("Discord bot token is not configured")}")
    }
    scenario("infra-23", "Microservice-Neustart: Clients verbinden neu") {
        requireDestructive()
        on(PAPER, admin)
        net.restart("player-microservice"); sleep(20000)
        on(PAPER, admin)
        note(answerContains(admin, "friend list", "Freund"))
        note(answerContains(admin, "settings info chat:pings", "Die Einstellung"))
        note(answer(admin, "clan list"))
        note(answerContains(admin, "playtime", "Spielzeit"))
    }
    scenario("infra-24", "Redis-Neustart: Reconnect") {
        requireDestructive()
        net.restart("redis"); sleep(15000)
        on(PAPER, admin); on(LOBBY, p1)
        val t = uniq(); val m = p1.mark(); admin.cmd("msg ${P1} $t")
        p1.expect(t, since = m, timeout = 10000)
        note("Nach Redis-Neustart: PN Paper→Lobby zugestellt")
    }
    scenario("infra-25", "RabbitMQ-Neustart: Reconnect") {
        requireDestructive()
        net.restart("rabbitmq"); sleep(30000)
        val out = net.console(PAPER, "clan list", 4000)
        note("Paper-Konsole „clan list“ nach RabbitMQ-Neustart: „${out.lines().filter { it.isNotBlank() }.takeLast(3).joinToString(" ⏎ ").take(200)}“")
    }
    scenario("infra-26", "Microservice offline: Fehlerverhalten") {
        requireDestructive()
        on(PAPER, admin)
        net.compose("stop", "player-microservice")
        try {
            val a = runCatching { answer(admin, "friend list", 8000) }.getOrElse { "keine Antwort" }
            note("Bei gestopptem Microservice: /friend list → „${a.take(150)}“")
        } finally {
            net.compose("start", "player-microservice"); sleep(20000)
        }
        note(answerContains(admin, "friend list", "Freund"))
    }
    scenario("infra-27", "Paper-Shutdown sauber") {
        requireDestructive()
        restartPaper()
        val log = net.logs("paper", 3000)
        check(!log.contains("Failed to disable feature")) { "„Failed to disable feature“ im Log" }
        note("Kein „Failed to disable feature“ beim Neustart")
    }
    scenario("infra-28", "Minestom-Shutdown sauber") {
        requireDestructive()
        restartServices("minestom")
        check(!net.logs("minestom", 3000).contains("Failed to disable feature")) { "„Failed to disable feature“ im Log" }
        note("Kein „Failed to disable feature“")
    }
    scenario("infra-29", "Serverwechsel Paper → Minestom → Paper") {
        on(PAPER, p1); on(LOBBY, p1); on(PAPER, p1)
        val f = footer(p1)
        check(f.contains("paper")) { "Footer „$f“" }
        check(p1.teams.isNotEmpty()) { "Keine Nametag-Teams" }
        note("Nach Paper→Lobby→Paper: Footer „${f.lines().firstOrNull { it.contains("befindest") }?.trim()}“, ${p1.teams.size} Teams, keine Fehler")
    }
    scenario("infra-30", "Persistenz nach Komplett-Neustart") { requireDestructive(); skip("Kompletter Netzwerk-Neustart nicht im Lauf") }
    scenario("infra-31", "Friend-Cooldowns: Aufräumen beim Start") {
        requireDestructive()
        net.sql("""INSERT INTO "surf-friends".friend_request_cooldowns (sender_uuid, receiver_uuid, decline_count, withdraw_count, expires_at) VALUES ('${offlineUuid("${P2}")}', '${offlineUuid("${A}")}', 1, 0, (now() at time zone 'utc') - interval '40 days')""")
        net.restart("player-microservice"); sleep(20000)
        val rows = net.sql("""SELECT count(*) FROM "surf-friends".friend_request_cooldowns WHERE sender_uuid = '${offlineUuid("${P2}")}'""")
        check(rows[0][0] == "0") { "Abgelaufener Cooldown nicht gelöscht" }
        note("Abgelaufener Cooldown beim Start gelöscht")
    }
    scenario("infra-32", "Velocity: Settings-Cache beim Login") {
        val since = java.time.Instant.now()
        quit(p1); join(p1); sleep(2000)
        val log = net.logsSince("velocity-bots", since)
        check(!log.contains("Failed to cache settings")) { "Warnung im Log" }
        note("Keine „Failed to cache settings“-Warnung beim Login von ${P1}")
    }
    scenario("infra-33", "Placeholder-Expansion geladen") {
        val p = net.logsOfCurrentRun("paper"); val v = net.logsOfCurrentRun("velocity")
        note("Paper erwähnt Expansion clan: ${p.contains("clan", true) && p.contains("expansion", true)}; Velocity: ${v.contains("clan", true) && v.contains("expansion", true)}")
    }
    scenario("infra-34", "LuckPerms-Änderung propagiert") {
        on(PAPER, admin, p1)
        lp("user ${p1.uuid} meta setprefix 100 <red>[Test]")
        try {
            val tab = eventually(8000, "Tab-Prefix") { tabDisplay(admin, p1).takeIf { it.contains("[Test]") } }
            val tag = eventually(8000, "Nametag-Prefix") { admin.teamOf("${P1}")?.prefix?.let { dev.slne.surf.player.tests.bot.Text.plain(it) }?.takeIf { it.contains("[Test]") } }
            on(LOBBY, admin, p1); sleep(1500)
            val lobby = tabDisplay(admin, p1)
            note("Paper Tab „$tab“, Nametag „$tag“; Lobby Tab „$lobby“")
            if (!lobby.contains("[Test]")) bad("Minestom-Tab ohne Prefix: „$lobby“; ${notes.joinToString()}")
        } finally {
            lp("user ${p1.uuid} meta removeprefix 100"); sleep(1000)
        }
    }
    scenario("infra-35", "Redis: nur Schlüssel unter surf-player:*") {
        val keys = net.redis("--scan", "--count", "1000").lines().filter { it.isNotBlank() }
        val old = keys.filter { k -> listOf("surf-chat:", "surf-vanish:", "surf-clan:", "surf-friends:", "surf-queue:", "surf-settings:").any { k.startsWith(it) } || k.endsWith("_v2") || k.contains(":v3:") }
        check(old.isEmpty()) { "Alte Schlüssel: ${old.take(5)}" }
        note("${keys.size} Schlüssel, keine alten Präfixe; surf-player-Gruppen: ${keys.filter { it.contains("surf-player:") }.map { it.substringAfter("surf-player:").substringBefore(':') }.distinct()}")
    }
    scenario("infra-36", "RabbitMQ: nur Queue surf-player") {
        val queues = Regex("\"name\":\"([^\"]*)\"").findAll(net.rabbit("queues")).map { it.groupValues[1] }.filter { it.startsWith("surf-player") }.toList()
        val other = queues.filter { it != "surf-player" && !it.startsWith("surf-player.callback.") }
        check("surf-player" in queues && other.isEmpty()) { "Queues: $queues" }
        note("surf-player + ${queues.count { it.startsWith("surf-player.callback.") }} Callback-Queues")
    }
    scenario("infra-37", "LuckPerms: default hat Spielerbefehle") {
        val d = net.velocity("lpv group default permission info")
        val a = net.velocity("lpv group admin permission info")
        check(d.contains("surf.player.command.*")) { "default: ${d.take(200)}" }
        check(a.contains("*")) { "admin: ${a.take(200)}" }
        note("default: surf.player.command.*; admin: *")
    }
    scenario("infra-38", "LuckPerms: DevPlayer1 ohne Team-, Admin- und Perk-Rechte") {
        val checks = listOf("surf.player.command.clan.revoke" to "true", "surf.player.command.friend.add" to "true",
            "surf.player.admin" to "undefined", "surf.player.team.spy" to "undefined", "surf.player.team.vanish" to "undefined",
            "surf.player.clan.color.foreground" to "undefined", "surf.player.playtime.others" to "undefined", "surf.player.vanish.bypass" to "undefined")
        val wrong = checks.mapNotNull { (node, want) ->
            val out = net.velocity("lpv user ${offlineUuid(P1)} permission check $node")
            if (out.contains(want, true)) null else "$node: ${out.lines().firstOrNull { it.contains(node) || it.contains("result", true) }?.trim() ?: out.take(80)}"
        }
        check(wrong.isEmpty()) { wrong.joinToString("; ") }
        note("Spielerbefehle true, Team/Admin/Perks undefined")
    }
    scenario("infra-39", "/surfplayer-Übersicht") {
        on(PAPER, admin)
        val m = admin.run("surfplayer")
        val r = admin.expect("Administration", since = m)
        sleep(800)
        val all = admin.messagesSince(m)
        val hover = all.flatMap { it.hovers }.firstOrNull { it.second.contains("Klicke, um die Aktionen anzuzeigen") }
        val click = all.flatMap { it.clicks }.firstOrNull { it.value.startsWith("/surfplayer ") }
        check(hover != null && click != null) { "Hover/Klick fehlt: „${r.plain.take(200)}“" }
        note("„${r.plain.trim().replace("\n", " ⏎ ").take(250)}“; Klick ${click!!.value}")
    }
    scenario("infra-40", "/surfplayervelocity-Übersicht") {
        on(LOBBY, admin)
        note(helpContains(admin, "surfplayervelocity", "Administration", "settings", "clan", "connection", "queue"))
    }
    scenario("infra-41", "Alte Admin-Wurzeln entfernt") {
        on(PAPER, admin)
        val roots = listOf("surfchat", "sc", "squeue", "playtimeadmin", "surftab", "surfsettings", "minimapcompanion", "surfcontentcreator", "squeue-velocity", "surfsocial")
        val present = roots.filter { it in admin.commandRoots.map { r -> r.lowercase() } }
        check(present.isEmpty()) { "Noch vorhanden: $present" }
        note("Keine der alten Wurzeln im Befehlsbaum")
    }
    scenario("infra-42", "Alte Befehle und Varianten entfernt") {
        on(PAPER, admin)
        // /clanchat stays: it is a documented shortcut of /clan chat (docs/command-conventions.md, clan-108).
        val gone = listOf("nick", "f").filter { it in admin.commandRoots }
        check(gone.isEmpty()) { "Noch vorhanden: $gone" }
        val accepted = mutableListOf<String>()
        for (cmd in listOf("clan admin reload", "clan player settings invite", "clan options tagcolor foreground FF0000", "clan setdiscord NULL", "clan disband", "friend decline ${P1}", "spy #clear", "slowchat #status", "slowchat on", "ignore #list", "settings chat:pings #info", "vanish reload")) {
            val m = admin.run(cmd)
            val r = admin.firstMessage(m, 3000)
            if (r == null || !(r.plain.contains("command.unknown") || r.plain.contains("Incorrect argument") || r.plain.contains("Unknown"))) accepted += "/$cmd → „${r?.plain?.trim()?.take(80)}“"
        }
        check(accepted.isEmpty()) { "Nicht abgelehnt: $accepted" }
        note("nick, f unbekannt; 12 alte Varianten abgelehnt")
    }
    scenario("infra-43", "Hilfe-Zeilen klickbar") {
        on(PAPER, p1)
        val m = p1.run("friend")
        sleep(1200)
        val all = p1.messagesSince(m)
        val hover = all.flatMap { it.hovers }.firstOrNull { it.second.contains("Klicke, um den Befehl einzufügen") }
        val click = all.flatMap { it.clicks }.firstOrNull { it.value == "/friend add " }
        check(hover != null && click?.action?.contains("suggest", true) == true) { "Hover/Klick: ${all.flatMap { it.clicks }.map { it.action + ":" + it.value }.take(5)}" }
        note("Hover „${hover!!.second}“, Klick fügt „/friend add “ ein")
    }
    scenario("infra-44", "Einstellungen netzwerkweit synchron") {
        on(PAPER, p1); on(LOBBY, admin)
        setSetting(p1, "chat:pings", false)
        try {
            on(LOBBY, p1)
            val m = p1.run("settings info chat:pings")
            val r = p1.expect("hat den Wert", since = m)
            check(r.plain.contains("false")) { "Lobby: „${r.plain.trim()}“" }
            val m2 = p1.mark(); admin.chat("${P1} ${uniq()}"); sleep(1500)
            val ping = p1.since(m2).any { it.kind == dev.slne.surf.player.tests.bot.Received.Kind.SOUND && it.extra.contains("chicken") }
            check(!ping) { "Ping-Sound trotz chat:pings aus in der Lobby" }
            note("Lobby sofort: „${r.plain.trim()}“, kein Ping")
        } finally { setSetting(p1, "chat:pings", true) }
    }
}
