package dev.slne.surf.player.core.client.minimap.platform

import net.kyori.adventure.key.Key
import java.util.*

/**
 * A player as the shared companion logic sees them, bound to the world they are in.
 */
interface MinimapPlayer {
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

    /**
     * How many status effects the client's Minecraft has, which is where the IDs of the effects mods add start,
     * or `null` if the client may run another version than the server.
     */
    val vanillaEffectCount: Int?

    /**
     * Gives the client the status effect with [networkId] until [removeClientEffect], hidden from its HUD.
     * Only the client knows the effect: it is for effects mods add, which the server has never heard of.
     */
    fun addClientEffect(networkId: Int)

    fun removeClientEffect(networkId: Int)
}
