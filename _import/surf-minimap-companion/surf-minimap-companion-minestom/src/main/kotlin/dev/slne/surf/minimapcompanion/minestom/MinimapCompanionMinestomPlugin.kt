package dev.slne.surf.minimapcompanion.minestom

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
import dev.slne.surf.minimapcompanion.minestom.listener.CompanionListener

object MinimapCompanionMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        CompanionConfig.init()
    }

    override suspend fun onEnable() {
        CompanionListener.register(eventNode)
    }
}
