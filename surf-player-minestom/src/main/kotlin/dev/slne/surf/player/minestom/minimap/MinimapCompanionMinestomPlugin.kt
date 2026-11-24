package dev.slne.surf.player.minestom.minimap

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.minestom.minimap.command.companionCommand
import dev.slne.surf.player.minestom.minimap.listener.ChannelAnnounceListener
import dev.slne.surf.player.minestom.minimap.listener.CompanionListener

object MinimapCompanionMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        CompanionConfigs.init()
    }

    override suspend fun onEnable() {
        ChannelAnnounceListener.register(eventNode)
        CompanionListener.register(eventNode)
        companionCommand()
    }
}
