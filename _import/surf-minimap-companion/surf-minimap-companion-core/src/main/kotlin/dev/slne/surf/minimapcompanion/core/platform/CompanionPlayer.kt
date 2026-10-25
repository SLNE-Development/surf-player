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

    fun sendPluginMessage(channel: String, payload: ByteArray)
}
