package dev.slne.surf.player.paper.minimap.platform

import dev.slne.surf.player.core.client.minimap.platform.MinimapPlayer
import dev.slne.surf.player.paper.plugin
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * Wraps [player] in [world], which defaults to the world they are in now. A wrapper is made for one sync or tracker
 * run on the player's thread, so it reads the client's channels once.
 */
class PaperMinimapPlayer(
    private val player: Player,
    private val world: World = player.world,
) : MinimapPlayer {
    override val uuid get() = player.uniqueId
    override val worldKey get() = world.key
    override val worldIdentity get() = world.uid.toString()

    // Paper copies the whole channel set on every call.
    private val channels by lazy(LazyThreadSafetyMode.NONE) { player.listeningPluginChannels }

    override fun hasChannel(channel: String) = channel in channels

    override fun sendPluginMessage(channel: String, payload: ByteArray) {
        player.sendPluginMessage(plugin, channel, payload)
    }

    override val vanillaEffectCount get() = PaperClientEffects.vanillaEffectCount(player)

    override fun addClientEffect(networkId: Int) = PaperClientEffects.add(player, networkId)

    override fun removeClientEffect(networkId: Int) = PaperClientEffects.remove(player, networkId)
}
