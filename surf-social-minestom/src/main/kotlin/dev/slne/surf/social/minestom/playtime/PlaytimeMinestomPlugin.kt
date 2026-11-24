package dev.slne.surf.social.minestom.playtime

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.core.client.playtime.ClientPlaytimeInstance
import dev.slne.surf.social.core.client.playtime.config.playtimeConfig
import dev.slne.surf.social.core.common.playtime.service.PayCheckService
import dev.slne.surf.social.core.common.playtime.service.PlaytimeService
import dev.slne.surf.social.minestom.playtime.command.playtimeAdminCommand
import dev.slne.surf.social.minestom.playtime.command.playtimeCommand
import dev.slne.surf.social.minestom.playtime.listener.PlayerAfkListener
import dev.slne.surf.social.minestom.playtime.listener.PlayerConnectionListener
import dev.slne.surf.social.minestom.playtime.playtime.playtimeTasks

object PlaytimeMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        PayCheckService.create(playtimeConfig)

        ClientPlaytimeInstance.clientLoader.onLoad()

        playtimeTasks.startAll()
    }

    override suspend fun onEnable() {
        ClientPlaytimeInstance.clientLoader.onEnable()
        playtimeCommand()
        playtimeAdminCommand()

        PlayerConnectionListener.register(eventNode)
        PlayerAfkListener.register(eventNode)
    }

    override suspend fun onDisable() {
        playtimeTasks.stopAll()
        PlaytimeService.flushAll()

        ClientPlaytimeInstance.clientLoader.onDisable()
    }
}
