package dev.slne.surf.player.tests.dsl

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.bot.Text
import dev.slne.surf.player.tests.bot.waitFor
import java.util.concurrent.ConcurrentHashMap

/**
 * One bot group: an admin and two normal players. Group 1 is DevAdmin/DevPlayer1/DevPlayer2;
 * group n ≥ 2 is DevA<n>/DevP<n>a/DevP<n>b (no name is a substring of another).
 */
data class BotGroup(val index: Int) {
    val admin = if (index == 1) "DevAdmin" else "DevA$index"
    val player1 = if (index == 1) "DevPlayer1" else "DevP${index}a"
    val player2 = if (index == 1) "DevPlayer2" else "DevP${index}b"
    val names get() = listOf(admin, player1, player2)
}

/**
 * The three test accounts of one group. Bots connect lazily on first use and stay connected between
 * scenarios; [serverOf] tracks which backend each one is on ("lobby" after joining the network).
 */
class Bots(private val host: String, private val port: Int, val group: BotGroup = BotGroup(1)) {
    private val bots = ConcurrentHashMap<String, Bot>()
    private val servers = ConcurrentHashMap<String, String>()

    fun get(name: String): Bot = ensure(name)

    fun ensure(name: String): Bot {
        val bot = bots.getOrPut(name) { Bot(name, host, port) }
        if (!bot.connected) {
            // Velocity's login rate limit may hit parallel groups; retry a few times
            var attempt = 0
            while (true) {
                try {
                    bot.connect(); break
                } catch (e: IllegalStateException) {
                    if (++attempt >= 5 || e.message?.contains("too fast") != true) throw e
                    Thread.sleep(3500L * attempt)
                }
            }
            servers[name] = "lobby"
            // let join messages, the command tree and the tab list arrive
            waitFor(3000) { if (bot.commandRoots.isNotEmpty() && bot.footer != null) true else null }
            Thread.sleep(300)
        }
        return bot
    }

    fun isOnline(name: String) = bots[name]?.connected == true

    fun online(): List<Bot> = bots.values.filter { it.connected }

    fun quit(bot: Bot) {
        bot.disconnect()
        servers.remove(bot.name)
        Thread.sleep(500)
    }

    fun serverOf(bot: Bot): String = servers[bot.name] ?: "lobby"

    /** The server named in the tab footer ("Du befindest dich auf <server>"), if known. */
    fun footerServer(bot: Bot): String? =
        Regex("Du befindest dich auf (\\S+)").find(Text.plain(bot.footer))?.groupValues?.get(1)

    fun moveTo(bot: Bot, server: String) {
        ensure(bot.name)
        // the footer is the truth (Velocity may have moved the bot, e.g. after a backend restart)
        footerServer(bot)?.let { servers[bot.name] = it }
        if (servers[bot.name] == server) return
        val before = bot.loginCount
        val mark = bot.mark()
        bot.cmd("server $server")
        waitFor(20_000) {
            when {
                bot.loginCount > before -> true
                bot.since(mark).any { it.plain.contains("bereits mit diesem Server verbunden") } -> true
                else -> null
            }
        } ?: throw IllegalStateException("${bot.name} could not switch to $server: ${bot.events.takeLast(3)}")
        servers[bot.name] = server
        // wait until the new server sent its tab footer and command tree instead of a fixed pause
        waitFor(4000) { if (footerServer(bot) == server) true else null }
        Thread.sleep(400)
    }

    fun shutdown() = bots.values.forEach { runCatching { it.disconnect() } }
}
