package dev.slne.surf.player.paper.vanish.util

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Reads the spectate mode hotkeys from a player's sneak input: "2x sneak" (teleport to the current
 * target) and "sneak + swap offhand" (back to the previous target).
 *
 * The client sends its sneak key state with every input packet, also while the key is held and
 * another key changes, so only a press (released to held) counts as a sneak. A double sneak is two
 * presses within [doubleSneakWindowMillis]; "back" is a swap while the sneak key is held, so a swap
 * shortly after a double sneak is an ordinary "next".
 */
class SpectateHotkeys(private val doubleSneakWindowMillis: Long = 500) {
    private val sneaking = ConcurrentHashMap.newKeySet<UUID>()
    private val lastPress = ConcurrentHashMap<UUID, Long>()

    /**
     * Records the sneak key state [shift] of an input packet of [player] at [nowMillis]; `true` when
     * this completes a double sneak.
     */
    fun onInput(player: UUID, shift: Boolean, nowMillis: Long): Boolean {
        if (!shift) {
            sneaking.remove(player)
            return false
        }

        if (!sneaking.add(player)) return false

        val previous = lastPress.put(player, nowMillis)
        if (previous != null && nowMillis - previous < doubleSneakWindowMillis) {
            lastPress.remove(player)
            return true
        }

        return false
    }

    /** Whether [player] is holding the sneak key, which turns a swap into "back". */
    fun isSneaking(player: UUID): Boolean = player in sneaking

    /** Forgets [player], e.g. when they leave or stop spectating. */
    fun forget(player: UUID) {
        sneaking.remove(player)
        lastPress.remove(player)
    }
}
