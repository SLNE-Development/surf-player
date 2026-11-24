package dev.slne.surf.player.minestom.minimap

import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.PlayerMinestomPlugin
import dev.slne.surf.player.minestom.command.minimapAdminActions
import dev.slne.surf.player.minestom.minimap.listener.ChannelAnnounceListener
import dev.slne.surf.player.minestom.minimap.listener.CompanionListener
import net.minestom.server.entity.Player

/**
 * The former surf-minimap-companion Minestom plugin.
 */
object MinimapMinestomFeature : MinestomPlayerFeature {
    override val id = "minimap"

    override suspend fun onLoad() {
        CompanionConfigs.init()
    }

    override suspend fun onEnable() {
        ChannelAnnounceListener.register(PlayerMinestomPlugin.eventNode)
        CompanionListener.register(PlayerMinestomPlugin.eventNode)
    }

    override fun adminActions() = minimapAdminActions()

    override fun onPlayerJoin(player: Player) {
        CompanionListener.onJoin(player)
    }

    override fun onPlayerQuit(player: Player) {
        CompanionListener.onQuit(player)
    }
}
