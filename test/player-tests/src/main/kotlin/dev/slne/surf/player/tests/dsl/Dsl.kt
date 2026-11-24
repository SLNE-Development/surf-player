package dev.slne.surf.player.tests.dsl

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.bot.Received
import dev.slne.surf.player.tests.bot.Text
import dev.slne.surf.player.tests.bot.waitFor
import dev.slne.surf.player.tests.infra.Network

enum class Status { ok, bad, odd, skip }

/** Ends a scenario with a verdict. */
class Verdict(val status: Status, val note: String) : RuntimeException(note, null, false, false)

/**
 * [isolated] scenarios only touch and observe the state of their own bot group and may run in
 * parallel with other groups; all others are `global` and run alone (one group online).
 */
class Scenario(val id: String, val feature: String, val title: String, val body: Ctx.() -> Unit) {
    val isolated: Boolean get() = Isolation.isIsolated(id)
}

/** All scenarios, registered by the files in `scenarios/`. One scenario per test-plan row id. */
object Registry {
    val all = LinkedHashMap<String, Scenario>()

    fun featureOf(id: String): String = when {
        id.startsWith("cc-") -> "contentcreator"
        id.startsWith("conn-") -> "connection"
        id.startsWith("ph-") -> "placeholders"
        else -> id.substringBefore('-')
    }
}

/** Registers the scenario for test-plan row [id]. */
fun scenario(id: String, title: String = "", body: Ctx.() -> Unit) {
    require(id !in Registry.all) { "duplicate scenario $id" }
    Registry.all[id] = Scenario(id, Registry.featureOf(id), title, body)
}

/** Registers a row a bot cannot check, with the (German) reason. */
fun notAutomatable(id: String, reason: String) = scenario(id) { skip(reason) }

/** Prefix glyph classes of surf-api messages, distinguished by their colour. */
enum class Prefix(val color: String) {
    Info("#97B3F7"), Success("#65FF64"), Warning("#FFA64D"), Error("#EE3D51"), Any("")
}

/**
 * Everything a scenario can use: the three bots (DevAdmin = NotAmmo in the plan, DevPlayer1 = FPNS,
 * DevPlayer2 = third account), the Docker network and assertion helpers. Failed expectations end
 * the scenario as `bad` with what the bot actually received.
 */
class Ctx(val net: Network, private val bots: Bots, val scenario: Scenario) {
    val notes = ArrayList<String>()

    /** Bot group of this run (1 = DevAdmin/DevPlayer1/DevPlayer2). */
    val group: Int get() = bots.group.index
    /** Names of the group's admin, player 1 and player 2. */
    val A: String get() = bots.group.admin
    val P1: String get() = bots.group.player1
    val P2: String get() = bots.group.player2
    /** Clan name/tag owned by the group's admin, and a second clan for player 2. */
    val CN: String get() = if (group == 1) "Testclan" else "Testclan$group"
    val CT: String get() = if (group == 1) "TCL" else "TC$group"
    val CN2: String get() = if (group == 1) "Andere" else "Andere$group"
    val CT2: String get() = if (group == 1) "AND" else "AN$group"

    val DevAdmin: Bot get() = bots.get(A)
    val DevPlayer1: Bot get() = bots.get(P1)
    val DevPlayer2: Bot get() = bots.get(P2)
    val admin get() = DevAdmin
    val p1 get() = DevPlayer1
    val p2 get() = DevPlayer2

    fun ok(note: String = notes.joinToString("; ")): Nothing = throw Verdict(Status.ok, note)
    fun bad(note: String): Nothing = throw Verdict(Status.bad, note)
    fun odd(note: String): Nothing = throw Verdict(Status.odd, note)
    fun skip(note: String): Nothing = throw Verdict(Status.skip, note)

    fun note(text: String) { notes += text }

    // ------------------------------------------------------------------------------------------
    // servers and connections
    // ------------------------------------------------------------------------------------------

    /** Moves the bots to [server] ("lobby" = Minestom, "paper"). */
    fun on(server: String, vararg who: Bot) = who.forEach { bots.moveTo(it, server) }

    fun serverOf(bot: Bot) = bots.serverOf(bot)

    /** Disconnects [bot] (it is reconnected on next use, landing in the lobby). */
    fun quit(bot: Bot) = bots.quit(bot)

    fun join(bot: Bot) = bots.ensure(bot.name)

    /** Disconnects every bot (before restarting servers). */
    fun quitAll() = bots.online().forEach { bots.quit(it) }

    fun isOnline(bot: Bot) = bots.isOnline(bot.name)

    // ------------------------------------------------------------------------------------------
    // messages
    // ------------------------------------------------------------------------------------------

    /** Runs [command] and returns the event mark before it, for [expect] calls. */
    fun Bot.run(command: String): Int {
        val m = mark()
        cmd(command)
        return m
    }

    fun Bot.say(message: String): Int {
        val m = mark()
        chat(message)
        return m
    }

    /**
     * Waits for a chat/system message containing all [containing] parts (case-sensitive) received
     * after [since]; fails the scenario with the received messages otherwise.
     */
    fun Bot.expect(
        vararg containing: String,
        since: Int = 0,
        prefix: Prefix = Prefix.Any,
        timeout: Long = 6000,
        kind: Received.Kind? = null,
    ): Received {
        val hit = await(timeout, since) { r ->
            (if (kind != null) r.kind == kind else r.kind == Received.Kind.SYSTEM || r.kind == Received.Kind.CHAT) &&
                containing.all { Text.has(r.plain, it) }
        } ?: bad(
            "$name: erwartet ${containing.joinToString(" + ") { "„$it“" }}, erhalten: ${observed(this, since, kind)}"
        )
        if (prefix != Prefix.Any && !hasPrefix(hit, prefix)) {
            odd("$name: „${hit.plain.trim()}“ kam mit anderem Präfix als $prefix (${Text.json(hit.component).take(200)})")
        }
        return hit
    }

    fun hasPrefix(r: Received, prefix: Prefix): Boolean =
        r.component?.let { Text.prefixColor(it) }?.equals(prefix.color, ignoreCase = true) == true

    /** Fails if a matching message arrives within [within] ms after [since]. */
    fun Bot.expectNone(vararg containing: String, since: Int, within: Long = 3000, kind: Received.Kind? = null) {
        val hit = await(within, since) { r ->
            (if (kind != null) r.kind == kind else r.kind == Received.Kind.SYSTEM || r.kind == Received.Kind.CHAT) &&
                // LuckPerms log lines reach every admin and mention other groups' changes
                !r.plain.startsWith("[LP]") &&
                containing.all { Text.has(r.plain, it) }
        }
        if (hit != null) bad("$name: unerwartet erhalten „${hit.plain.trim()}“")
    }

    /** First message after [since] (any text), or null. */
    fun Bot.firstMessage(since: Int, timeout: Long = 5000): Received? =
        await(timeout, since) { it.kind == Received.Kind.SYSTEM || it.kind == Received.Kind.CHAT }

    fun Bot.expectActionBar(vararg containing: String, since: Int = 0, timeout: Long = 5000) =
        expect(*containing, since = since, timeout = timeout, kind = Received.Kind.ACTIONBAR)

    fun Bot.expectSound(key: String, since: Int, timeout: Long = 4000): Received =
        await(timeout, since) { it.kind == Received.Kind.SOUND && it.extra.contains(key, true) }
            ?: bad("$name: kein Sound „$key“, Sounds: ${since(since).filter { it.kind == Received.Kind.SOUND }.map { it.extra }}")

    fun Bot.expectNoSound(key: String, since: Int, within: Long = 3000) {
        Thread.sleep(within)
        since(since).firstOrNull { it.kind == Received.Kind.SOUND && it.extra.contains(key, true) }
            ?.let { bad("$name: unerwarteter Sound ${it.extra}") }
    }

    /** The command is not available: it is missing from the client's command tree. */
    fun Bot.expectCommandUnavailable(root: String) {
        Thread.sleep(300)
        if (root in commandRoots) {
            val m = run(root)
            val r = firstMessage(m, 3000)
            bad("$name: /$root steht im Befehlsbaum; Ausführung: „${r?.plain?.trim() ?: "keine Antwort"}“")
        }
    }

    fun Bot.expectCommandAvailable(root: String) {
        Thread.sleep(300)
        if (root !in commandRoots) bad("$name: /$root fehlt im Befehlsbaum (${commandRoots.size} Befehle)")
    }

    /** Clicks the click event whose node text contains [text] in [message]. */
    fun Bot.clickIn(message: Received, text: String): Text.Click {
        // the innermost node wins (a whole message can carry a suggest_command around a button)
        val click = message.clicks.filter { Text.has(it.text, text) }.minByOrNull { it.text.length }
            ?: bad("$name: kein klickbares „$text“ in „${message.plain.trim()}“ (Klicks: ${message.clicks.map { it.text to it.value }})")
        click(click.event)
        return click
    }

    fun observed(bot: Bot, since: Int, kind: Received.Kind? = null): String {
        val list = bot.since(since).filter {
            if (kind != null) it.kind == kind else it.kind == Received.Kind.SYSTEM || it.kind == Received.Kind.CHAT
        }.map { "„" + it.plain.trim().replace("\n", " ⏎ ").take(220) + "“" }
        return if (list.isEmpty()) "nichts" else list.takeLast(6).joinToString(", ")
    }

    fun <T : Any> eventually(timeout: Long = 5000, what: String, supplier: () -> T?): T =
        waitFor(timeout, supplier) ?: bad("Zeitüberschreitung: $what")

    fun check(condition: Boolean, failure: () -> String) {
        if (!condition) bad(failure())
    }

    fun sleep(ms: Long) = Thread.sleep(ms)
}
