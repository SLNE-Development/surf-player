package dev.slne.surf.minimapcompanion.minestom

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.minestom.command.minimapCompanionCommand
import dev.slne.surf.minimapcompanion.minestom.listener.CompanionListener

object MinimapCompanionMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        CompanionConfigs.init()
    }

    override suspend fun onEnable() {
        CompanionListener.register(eventNode)
        minimapCompanionCommand()
    }
}
