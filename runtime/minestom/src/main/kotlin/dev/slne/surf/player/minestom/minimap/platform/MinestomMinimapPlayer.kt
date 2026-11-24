package dev.slne.surf.player.minestom.minimap.platform

import dev.slne.surf.player.core.client.minimap.platform.MinimapPlayer
import net.kyori.adventure.key.Key
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance

/**
 * Wraps [player] in [instance]. Messages reach only the channels the client registered.
 */
class MinestomMinimapPlayer(
    private val player: Player,
    private val instance: Instance,
) : MinimapPlayer {
    override val uuid get() = player.uuid
    override val worldKey get() = Key.key(instance.dimensionName)
    override val worldIdentity get() = instance.dimensionName

    override fun hasChannel(channel: String) = RegisteredChannels.isRegistered(uuid, channel)

    override fun sendPluginMessage(channel: String, payload: ByteArray) {
        if (hasChannel(channel)) {
            player.sendPluginMessage(channel, payload)
        }
    }

    override val vanillaEffectCount get() = MinestomClientEffects.vanillaEffectCount

    override fun addClientEffect(networkId: Int) = MinestomClientEffects.add(player, networkId)

    override fun removeClientEffect(networkId: Int) = MinestomClientEffects.remove(player, networkId)
}
