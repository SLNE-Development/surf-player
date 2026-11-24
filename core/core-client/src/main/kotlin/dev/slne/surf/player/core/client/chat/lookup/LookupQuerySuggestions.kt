package dev.slne.surf.player.core.client.chat.lookup

import dev.slne.surf.player.api.chat.message.MessageType

/**
 * The tab completion of `/surfplayer chat lookup [--filter wert ...]`, whose filters are typed as
 * one greedy string. Completes the filter names and, after a filter name, its values.
 *
 * Every suggestion is the whole typed query with its last word completed, since the greedy string
 * is replaced as a whole.
 */
object LookupQuerySuggestions {
    /** The filter names, in the order they are suggested. */
    val filterKeys = listOf(
        "--sender", "--receiver", "--type", "--range", "--server", "--message", "--deleted",
        "--deletedBy", "--messageUuid", "--limit", "--page",
    )

    /** The message types suggested for `--type`. */
    val messageTypes = listOf(MessageType.GLOBAL, MessageType.DIRECT, MessageType.TEAM, MessageType("CLAN_CHAT"))
        .map { it.value.lowercase() }

    val ranges = listOf("10m", "1h", "6h", "12h", "1d", "3d", "7d", "30d")
    val booleans = listOf("true", "false")
    val limits = listOf("10", "25", "50", "100")
    val pages = listOf("1", "2", "3")

    private val playerKeys = setOf("--sender", "--receiver", "--deletedBy")

    /**
     * The completions of [input], the text typed so far for the query.
     *
     * @param playerNames the player names starting with a prefix (ignoring case), for `--sender`,
     * `--receiver` and `--deletedBy`
     * @param serverNames the servers known in the network, for `--server`
     */
    fun suggest(
        input: String,
        playerNames: (prefix: String) -> Collection<String>,
        serverNames: () -> Collection<String>,
    ): List<String> {
        val lastSpace = input.lastIndexOf(' ')
        val head = input.substring(0, lastSpace + 1)
        val current = input.substring(lastSpace + 1)
        val previous = head.trimEnd().substringAfterLast(' ')

        val candidates = when {
            head.count { it == '"' } % 2 == 1 -> emptyList() // inside a quoted --message value
            previous in filterKeys && previous != "--message" -> valuesOf(previous, current, playerNames, serverNames)
            current.isEmpty() || current.startsWith("-") -> filterKeys.filter { it !in usedKeys(head) }
            else -> emptyList()
        }

        return candidates
            .filter { it.startsWith(current, ignoreCase = true) }
            .map { head + it }
    }

    private fun valuesOf(
        key: String,
        current: String,
        playerNames: (String) -> Collection<String>,
        serverNames: () -> Collection<String>,
    ): Collection<String> = when (key) {
        in playerKeys -> playerNames(current)
        "--type" -> messageTypes
        "--range" -> ranges
        "--server" -> serverNames()
        "--deleted" -> booleans
        "--limit" -> limits
        "--page" -> pages
        else -> emptyList()
    }

    private fun usedKeys(head: String): Set<String> =
        head.split(' ').filterTo(HashSet()) { it.startsWith("--") }
}
