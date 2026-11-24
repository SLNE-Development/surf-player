package dev.slne.surf.player.tests.dsl

/**
 * Which scenarios may run in parallel bot groups. A scenario is `isolated` when it only changes
 * and asserts on the state of its own three bots (friends, own clan, ignore list, PMs, spy, own
 * settings, own playtime, menus) and every text it waits for is unique to the group. Everything
 * else is `global` (server-wide toggles, config edits and reloads, vanish, tab header counts,
 * queue, restarts, log checks) and runs in the serial phase with only group 1 online.
 * Unknown ids default to global.
 */
object Isolation {
    private fun ids(prefix: String, vararg ranges: IntRange) =
        ranges.flatMap { r -> r.map { n -> if (n < 10) "$prefix-0$n" else "$prefix-$n" } }

    private val isolated: Set<String> = buildSet {
        // chat: global chat with unique texts, PMs, ignore, spy, lookups, help lists
        addAll(ids("chat", 1..11, 13..14, 16..18, 20..24, 29..31, 33..34, 37..70, 80..91, 107..116, 121..121, 124..125, 127..127))
        // friends: everything except the vanish rows
        addAll(ids("friends", 1..48, 53..56))
        // clan: own clan per group (name/tag derived from the group); admin reload/cache are global
        addAll(ids("clan", 1..112, 115..134, 136..138))
        addAll(listOf("tab-16", "nametag-01", "nametag-02", "nametag-03", "nametag-05", "nametag-06", "nametag-16"))
        addAll(ids("settings", 1..18, 22..33, 36..47, 52..55, 57..61))
        addAll(ids("profile", 1..6))
        addAll(ids("playtime", 1..13, 16..16, 35..37))
        addAll(listOf("minimap-04", "minimap-05", "minimap-25", "minimap-26", "cc-05", "cc-06"))
        addAll(listOf("conn-01", "conn-03", "conn-04", "conn-05", "conn-06", "conn-07", "conn-08", "conn-09", "conn-11", "conn-13"))
    }

    fun isIsolated(id: String) = id in isolated
}
