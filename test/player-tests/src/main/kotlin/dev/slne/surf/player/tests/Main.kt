package dev.slne.surf.player.tests

import dev.slne.surf.player.tests.bot.Received
import dev.slne.surf.player.tests.bot.Text
import dev.slne.surf.player.tests.dsl.Bots
import dev.slne.surf.player.tests.dsl.Registry
import dev.slne.surf.player.tests.dsl.Status
import dev.slne.surf.player.tests.infra.Network
import dev.slne.surf.player.tests.scenarios.registerAll
import java.io.File
import kotlin.system.exitProcess

private const val USAGE = """
player-tests [options]
  --feature <name>     only scenarios of this feature (repeatable, comma separated)
  --id <id>            only these scenario ids (repeatable, comma separated, prefix* allowed)
  --failed             only scenarios that were bad/odd in the last report
  --plan <tests.json>  merge the results into this test plan
  --network-dir <dir>  docker/test-network directory (default: ../../docker/test-network)
  --host/--port        bot proxy (default 127.0.0.1:25566)
  --list               list the scenarios and exit
  --parallel <n>       bot groups for the isolated scenarios (default 6; 1 = fully serial)
  --wait-ready         wait until all servers are up before running
  --destructive        also run scenarios that stop/restart containers (disturbs human testers)
  --probe <bot> <server> <command...>  run commands as a bot and print everything it receives
"""

fun main(args: Array<String>) {
    val features = mutableSetOf<String>()
    val ids = mutableListOf<String>()
    var failed = false
    var plan: File? = null
    var networkDir = File("../../docker/test-network")
    var host = "127.0.0.1"
    var port = 25566
    var list = false
    var probe: List<String>? = null
    var waitReady = false
    var parallel = 6
    var i = 0
    while (i < args.size) {
        when (val a = args[i]) {
            "--feature" -> features += args[++i].split(',').map { it.trim() }
            "--id" -> ids += args[++i].split(',').map { it.trim() }
            "--failed" -> failed = true
            "--plan" -> plan = File(args[++i])
            "--network-dir" -> networkDir = File(args[++i])
            "--host" -> host = args[++i]
            "--port" -> port = args[++i].toInt()
            "--list" -> list = true
            "--destructive" -> System.setProperty("playertests.destructive", "true")
            "--wait-ready" -> waitReady = true
            "--parallel" -> parallel = args[++i].toInt()
            "--probe" -> { probe = args.drop(i + 1); i = args.size }
            "-h", "--help" -> { println(USAGE); return }
            else -> { System.err.println("unknown argument $a\n$USAGE"); exitProcess(2) }
        }
        i++
    }

    registerAll()
    val net = Network(networkDir.canonicalFile)
    val bots = Bots(host, port)
    val reportFile = File("build/report.json")

    if (probe != null) {
        runProbe(bots, probe)
        bots.shutdown()
        exitProcess(0)
    }

    val previousFailures = if (failed) Runner.previousFailures(reportFile) else emptySet()
    val selected = Registry.all.values.filter { s ->
        (features.isEmpty() || s.feature in features) &&
            (ids.isEmpty() || ids.any { p -> if (p.endsWith("*")) s.id.startsWith(p.dropLast(1)) else s.id == p }) &&
            (!failed || s.id in previousFailures)
    }

    if (list) {
        selected.forEach { println("${it.id}\t${it.feature}\t${it.title}") }
        println("${selected.size} scenarios")
        return
    }

    if (waitReady) {
        println("Waiting for the network (Paper, Minestom, microservice, proxies)...")
        net.waitReady(600)
    }
    println("Running ${selected.size} scenarios against $host:$port with $parallel bot groups (network ${net.dir})")
    provisionGroups(net, parallel)
    val runner = Runner(net, host, port, reportFile)
    val started = System.currentTimeMillis()
    val results = runner.run(selected, parallel)
    val wall = (System.currentTimeMillis() - started) / 1000
    val serial = results.sumOf { it.durationMs } / 1000

    plan?.let { runner.mergeIntoPlan(it, Runner.load(reportFile)) }

    val counts = Status.entries.associateWith { s -> results.count { it.status == s } }
    println()
    println("Summary: " + counts.entries.joinToString("  ") { "${it.key}=${it.value}" } + "  (report: ${reportFile.absolutePath})")
    println("Wall clock ${wall / 60}m ${wall % 60}s; sum of scenario durations (one group, serial) ${serial / 60}m ${serial % 60}s")
    results.filter { it.status == Status.bad || it.status == Status.odd }.forEach {
        println("  ${it.status.name.uppercase()} ${it.id}: ${it.note.replace('\n', ' ').take(300)}")
    }
    exitProcess(if (counts[Status.bad]!! > 0) 1 else 0)
}

/**
 * Puts every bot group (1..n) into a known LuckPerms state by offline UUID: leftovers of an aborted
 * run (temporary permission/meta nodes) are cleared, admins inherit `admin`, players only `default`.
 */
private fun provisionGroups(net: Network, parallel: Int) {
    for (i in 1..parallel) {
        val g = dev.slne.surf.player.tests.dsl.BotGroup(i)
        val uuid = { n: String -> java.util.UUID.nameUUIDFromBytes("OfflinePlayer:$n".toByteArray()) }
        for (n in g.names) {
            net.velocity("lpv user ${uuid(n)} permission clear")
            net.velocity("lpv user ${uuid(n)} meta clear")
        }
        net.velocity("lpv user ${uuid(g.admin)} parent add admin")
        for (p in listOf(g.player1, g.player2)) net.velocity("lpv user ${uuid(p)} parent set default")
    }
}

private fun runProbe(bots: Bots, args: List<String>) {
    val bot = bots.get(args[0])
    bots.moveTo(bot, args[1])
    for (command in args.drop(2)) {
        val m = bot.mark()
        if (command == "info") bot.resendClientInformation() else if (command.startsWith("/")) bot.cmd(command) else if (command.startsWith("sleep:")) Thread.sleep(command.substringAfter(':').toLong()) else bot.chat(command)
        Thread.sleep(2500)
        println("== $command")
        bot.since(m).forEach { r ->
            println("  $r")
            if (r.component != null && r.kind != Received.Kind.SOUND) println("     json=" + Text.json(r.component).take(8000))
        }
        bot.window?.let { w ->
            println("  window '${w.titleText}' ${w.type}")
            w.nonEmpty().forEach { (slot, item) -> println("    [$slot] $item") }
        }
    }
    println("roots: ${bot.commandRoots.sorted()}")
    println("header: ${Text.plain(bot.header)}")
    println("footer: ${Text.plain(bot.footer)}")
    bot.tab.values.forEach { println("tab: ${it.name} listed=${it.listed} order=${it.listOrder} display=${it.display} json=${Text.json(it.displayName)}") }
    bot.teams.values.forEach { println("team: ${it.name} prefix=${Text.json(it.prefix)} suffix=${Text.json(it.suffix)} vis=${it.visibility} players=${it.players}") }
    println("sees: ${bot.players.keys}")
}
