package dev.slne.surf.social.minestom.tab

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.core.client.tab.hook.ClanHook
import dev.slne.surf.social.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.social.core.client.tab.redis.redisLoader
import dev.slne.surf.social.core.client.tab.service.TablistService
import dev.slne.surf.social.minestom.tab.command.surfTabCommand
import dev.slne.surf.social.minestom.tab.hook.registerLuckPermsListeners
import dev.slne.surf.social.minestom.tab.listener.PlayerListener
import dev.slne.surf.social.minestom.tab.listener.PlaytimeListener
import dev.slne.surf.social.minestom.tab.platform.MinestomTabPlatform

object TabMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        redisLoader.connect()

        TablistService.start(MinestomTabPlatform())

        registerLuckPermsListeners()
        ClanHook.createListeners()
        ContentCreatorHook.registerListener()
    }

    override suspend fun onEnable() {
        surfTabCommand()

        PlayerListener.register(eventNode)
        PlaytimeListener.register(eventNode)
    }

    override suspend fun onDisable() {
        TablistService.stop()
        redisLoader.disconnect()
    }
}
