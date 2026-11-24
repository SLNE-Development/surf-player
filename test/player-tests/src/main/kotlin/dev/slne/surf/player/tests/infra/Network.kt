package dev.slne.surf.player.tests.infra

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Access to the Docker test network (docker/test-network): server consoles, logs, PostgreSQL,
 * Redis and the RabbitMQ management API. Everything goes through the docker CLI.
 */
class Network(val dir: File) {
    val project = "surf-player-test"
    private val env: Map<String, String> = File(dir, ".env").takeIf { it.isFile }?.readLines()
        ?.filter { '=' in it && !it.trimStart().startsWith("#") }
        ?.associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() } ?: emptyMap()

    data class Result(val exit: Int, val out: String)

    fun exec(vararg command: String, timeoutSec: Long = 120, stdin: String? = null): Result {
        val pb = ProcessBuilder(*command).directory(dir).redirectErrorStream(true)
        pb.environment()["MSYS_NO_PATHCONV"] = "1"
        val p = pb.start()
        if (stdin != null) p.outputStream.use { it.write(stdin.toByteArray()) } else p.outputStream.close()
        val out = StringBuilder()
        val reader = Thread { out.append(p.inputStream.bufferedReader(Charsets.UTF_8).readText()) }
        reader.start()
        if (!p.waitFor(timeoutSec, TimeUnit.SECONDS)) {
            p.destroyForcibly()
            reader.join(1000)
            return Result(-1, out.toString() + "\n<timeout>")
        }
        reader.join(5000)
        return Result(p.exitValue(), out.toString())
    }

    fun compose(vararg args: String, timeoutSec: Long = 300): Result =
        exec("docker", "compose", "-p", project, *args, timeoutSec = timeoutSec)

    // ------------------------------------------------------------------------------------------
    // consoles
    // ------------------------------------------------------------------------------------------

    /** Velocity (main proxy) console through RCON; returns the command output. */
    fun velocity(command: String): String = strip(compose("exec", "-T", "velocity", "rcon-cli", command).out)

    /**
     * Runs [command] on the console of [server] (paper or minestom) and returns what the server
     * logged in the following [waitMs] (Paper answers RCON asynchronously, Minestom only logs).
     */
    fun console(server: String, command: String, waitMs: Long = 2500): String {
        val since = Instant.now().minusMillis(300)
        val direct = when (server) {
            "paper" -> compose("exec", "-T", "paper", "rcon-cli", command).out
            "minestom", "lobby" -> compose("exec", "-T", "minestom", "console", command).out
            "velocity" -> compose("exec", "-T", "velocity", "rcon-cli", command).out
            else -> error("unknown server $server")
        }
        Thread.sleep(waitMs)
        val service = if (server == "lobby") "minestom" else server
        return strip(direct + "\n" + logsSince(service, since))
    }

    fun logsSince(service: String, since: Instant): String =
        strip(compose("logs", "--no-log-prefix", "--since", since.toString(), service).out)

    fun logs(service: String, tail: Int? = null): String =
        strip(compose("logs", "--no-log-prefix", *(tail?.let { arrayOf("--tail", "$it") } ?: emptyArray()), service).out)

    /** Start time of the current container of [service] (to restrict log checks to the current run). */
    fun startedAt(service: String): Instant? {
        val id = compose("ps", "-q", service).out.trim().lines().firstOrNull()?.trim().orEmpty()
        if (id.isEmpty()) return null
        val s = exec("docker", "inspect", "-f", "{{.State.StartedAt}}", id).out.trim()
        return runCatching { Instant.parse(s) }.getOrNull()
    }

    fun logsOfCurrentRun(service: String): String = startedAt(service)?.let { logsSince(service, it) } ?: logs(service)

    // ------------------------------------------------------------------------------------------
    // data stores
    // ------------------------------------------------------------------------------------------

    /** Runs SQL against database `surf`; rows as lists of columns ('|' separated). */
    fun sql(query: String, database: String = "surf"): List<List<String>> {
        // SQL goes through stdin: Windows' ProcessBuilder does not escape the double quotes of
        // schema names like "surf-chat" in arguments.
        val r = exec(
            "docker", "compose", "-p", project, "exec", "-T", "postgres",
            "psql", "-U", "surf", "-d", database, "-At", "-F", "|", "-v", "ON_ERROR_STOP=1", "-f", "-",
            stdin = query,
        )
        if (r.exit != 0) throw IllegalStateException("psql failed: ${r.out.trim()}")
        return r.out.lines().filter { it.isNotBlank() }.map { it.split('|') }
    }

    fun redis(vararg args: String): String =
        compose("exec", "-T", "redis", "redis-cli", "-a", env["REDIS_PASSWORD"] ?: "", "--no-auth-warning", *args).out.trim()

    fun rabbit(path: String): String {
        val port = env["RABBITMQ_MANAGEMENT_PORT"] ?: "15672"
        val auth = Base64.getEncoder().encodeToString("surf:${env["RABBITMQ_PASSWORD"]}".toByteArray())
        val req = HttpRequest.newBuilder(URI("http://127.0.0.1:$port/api/$path")).header("Authorization", "Basic $auth").build()
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body()
    }

    fun ls(service: String, path: String): String = compose("exec", "-T", service, "ls", "-R", path).out

    fun cat(service: String, path: String): String = compose("exec", "-T", service, "cat", path).out

    /** Overwrites a file inside a container (used to change plugin configs for one scenario). */
    fun write(service: String, path: String, content: String) {
        val r = exec("docker", "compose", "-p", project, "exec", "-T", service, "sh", "-c", "cat > '$path'", stdin = content)
        if (r.exit != 0) throw IllegalStateException("writing $path in $service failed: ${r.out}")
    }

    fun restart(vararg services: String) = compose("restart", *services, timeoutSec = 600)

    /** Waits until the bot proxy reports both backends reachable, i.e. a status ping and the logs say so. */
    fun waitReady(timeoutSec: Long = 300) {
        val end = System.currentTimeMillis() + timeoutSec * 1000
        while (System.currentTimeMillis() < end) {
            val paper = logsOfCurrentRun("paper").contains("Done (")
            val lobby = logsOfCurrentRun("minestom").contains("is ready in")
            val micro = logsOfCurrentRun("player-microservice").isNotBlank()
            if (paper && lobby && micro && ping("127.0.0.1", 25566) && ping("127.0.0.1", 25565)) return
            Thread.sleep(3000)
        }
        throw IllegalStateException("network not ready after ${timeoutSec}s")
    }

    private fun ping(host: String, port: Int) = runCatching {
        java.net.Socket().use { it.connect(java.net.InetSocketAddress(host, port), 2000) }; true
    }.getOrDefault(false)

    companion object {
        private val ansi = Regex("\u001B\\[[;\\d]*[ -/]*[@-~]")
        fun strip(s: String) = s.replace(ansi, "")
    }
}
