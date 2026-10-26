package dev.slne.surf.minimapcompanion.core.platform

import net.kyori.adventure.key.Key
import java.util.*

/**
 * A player as the shared companion logic sees them, bound to the world they are in.
 */
interface CompanionPlayer {
    val uuid: UUID

    /**
     * The key of the player's world, as Xaero shows it.
     */
    val worldKey: Key

    /**
     * What the level ID of the player's world is computed from.
     */
    val worldIdentity: String

    /**
     * Returns whether the client registered [channel], which tells the companion which of Xaero's mods it has.
     */
    fun hasChannel(channel: String): Boolean

    fun sendPluginMessage(channel: String, payload: ByteArray)
}
