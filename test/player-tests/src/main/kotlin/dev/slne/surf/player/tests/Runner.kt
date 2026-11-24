package dev.slne.surf.player.tests

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.slne.surf.player.tests.dsl.*
import dev.slne.surf.player.tests.infra.Network
import java.io.File

data class Result(val id: String, val feature: String, val status: Status, val note: String, val durationMs: Long)

/** Runs scenarios, writes build/report.json and optionally merges the results into a test plan. */
class Runner(
    private val net: Network,
    private val host: String,
    private val port: Int,
    private val reportFile: File,
) {
    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private val previous: List<Result> = load(reportFile)
    private val results = java.util.Collections.synchronizedList(ArrayList<Result>())
    private val done = java.util.concurrent.atomic.AtomicInteger()
    private var total = 0

    /**
     * Phase 1: the isolated scenarios on [parallel] bot groups at once (work queue). Phase 2: the
     * global scenarios one after another with only group 1 online.
     */
    fun run(scenarios: List<Scenario>, parallel: Int): List<Result> {
        total = scenarios.size
        val isolated = scenarios.filter { it.isolated }
        val global = scenarios.filterNot { it.isolated }
        val groups = (1..parallel.coerceAtLeast(1)).map { Bots(host, port, BotGroup(it)) }
        val started = System.currentTimeMillis()
        if (isolated.isNotEmpty()) {
            println("Phase 1: ${isolated.size} isolated scenarios on ${groups.size} bot groups")
            val queue = java.util.concurrent.ConcurrentLinkedQueue(isolated)
            val threads = groups.map { bots ->
                Thread {
                    while (true) {
                        val s = queue.poll() ?: break
                        runOne(s, bots)
                    }
                }.apply { name = "group-${bots.group.index}"; start() }
            }
            threads.forEach { it.join() }
            groups.drop(1).forEach { it.shutdown() }
            println("Phase 1 took ${(System.currentTimeMillis() - started) / 1000}s")
        }
        if (global.isNotEmpty()) {
            val t = System.currentTimeMillis()
            println("Phase 2: ${global.size} global scenarios with group 1 only")
            global.forEach { runOne(it, groups.first()) }
            println("Phase 2 took ${(System.currentTimeMillis() - t) / 1000}s")
        }
        groups.forEach { it.shutdown() }
        return results.toList()
    }

    private fun runOne(s: Scenario, bots: Bots) {
        val start = System.currentTimeMillis()
        val ctx = Ctx(net, bots, s)
        val (status, note) = try {
            s.body(ctx)
            Status.ok to ctx.notes.joinToString("; ").ifEmpty { "Wie erwartet." }
        } catch (v: Verdict) {
            v.status to v.note
        } catch (t: Throwable) {
            Status.bad to "Fehler im Testablauf: ${t.javaClass.simpleName}: ${t.message}"
        }
        // notes of other groups name their bots by role (DevAdmin/DevPlayer1/DevPlayer2) for the plan
        val g = bots.group
        val roleNote = if (g.index == 1) note else note
            .replace(g.player1, "DevPlayer1").replace(g.player2, "DevPlayer2").replace(g.admin, "DevAdmin")
        val r = Result(s.id, s.feature, status, "$roleNote${if (g.index == 1) "" else " [Gruppe ${g.index}]"}".take(1500), System.currentTimeMillis() - start)
        results += r
        synchronized(this) {
            println("[${done.incrementAndGet()}/$total g${bots.group.index}] ${s.id.padEnd(16)} ${status.name.padEnd(4)} ${note.replace('\n', ' ').take(180)}")
            writeReport(results.toList())
        }
    }

    fun writeReport(results: List<Result>) {
        reportFile.parentFile.mkdirs()
        // keep the results of scenarios not run this time (filtered runs, --failed)
        val merged = LinkedHashMap<String, Result>()
        previous.forEach { merged[it.id] = it }
        results.forEach { merged[it.id] = it }
        val arr = JsonArray()
        merged.values.forEach { r ->
            arr.add(JsonObject().apply {
                addProperty("id", r.id)
                addProperty("feature", r.feature)
                addProperty("status", r.status.name)
                addProperty("note", r.note)
                addProperty("durationMs", r.durationMs)
            })
        }
        reportFile.writeText(gson.toJson(arr), Charsets.UTF_8)
    }

    /** Writes `"result": {status, note, by: "bot"}` into every row of [plan] that has a result. */
    fun mergeIntoPlan(plan: File, results: List<Result>) {
        val root = JsonParser.parseString(plan.readText(Charsets.UTF_8)).asJsonObject
        val byId = results.associateBy { it.id }
        var merged = 0
        for (feature in root.getAsJsonArray("features")) {
            for (row in feature.asJsonObject.getAsJsonArray("tests")) {
                val o = row.asJsonObject
                val r = byId[o.get("id").asString] ?: continue
                o.add("result", JsonObject().apply {
                    addProperty("status", r.status.name)
                    addProperty("note", r.note)
                    addProperty("by", "bot")
                })
                merged++
            }
        }
        plan.writeText(gson.toJson(root), Charsets.UTF_8)
        println("Merged $merged results into $plan")
    }

    companion object {
        fun load(reportFile: File): List<Result> {
            if (!reportFile.isFile) return emptyList()
            return runCatching {
                JsonParser.parseString(reportFile.readText(Charsets.UTF_8)).asJsonArray.map { it.asJsonObject }.map {
                    Result(
                        it.get("id").asString, it.get("feature").asString, Status.valueOf(it.get("status").asString),
                        it.get("note").asString, it.get("durationMs").asLong,
                    )
                }
            }.getOrDefault(emptyList())
        }

        fun previousFailures(reportFile: File): Set<String> =
            load(reportFile).filter { it.status == Status.bad || it.status == Status.odd }.map { it.id }.toSet()
    }
}
