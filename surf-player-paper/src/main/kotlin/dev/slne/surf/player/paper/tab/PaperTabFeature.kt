package dev.slne.surf.player.paper.tab

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.player.core.client.tab.hook.ClanHook
import dev.slne.surf.player.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.player.core.client.tab.hook.LuckPermsHook
import dev.slne.surf.player.core.client.tab.redis.redisLoader
import dev.slne.surf.player.core.client.tab.service.TablistService
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.player.paper.tab.command.surfTabCommand
import dev.slne.surf.player.paper.tab.listener.PlayerListener
import dev.slne.surf.player.paper.tab.listener.PlaytimeListener
import dev.slne.surf.player.paper.tab.listener.VanishListener
import dev.slne.surf.player.paper.tab.platform.PaperTabPlatform

object PaperTabFeature : PaperPlayerFeature {
    override val name = "tab"

    override suspend fun onLoad() {
        redisLoader.register()
    }

    override suspend fun onEnable() {
        surfTabCommand()

        TablistService.start(PaperTabPlatform())
        subscribeToLuckPermsUserChanges(LuckPermsHook::updatePlayerInTablist)
        PlayerListener.register()

        if (isPlaytimeHook) {
            PlaytimeListener.register()
        }

        // Clans and content creators are bundled in the same plugin and enabled before tab.
        ClanHook.createListeners()
        ContentCreatorHook.registerListener()

        if (isVanishHook) {
            VanishListener.register()
        }
    }

    override suspend fun onDisable() {
        TablistService.stop()
    }
}

val isVanishHook get() = pluginManager.isPluginEnabled("surf-vanish-paper")
val isPlaytimeHook get() = pluginManager.isPluginEnabled("surf-playtime-paper")
