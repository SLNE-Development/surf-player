package dev.slne.surf.social.paper.tab

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.social.core.client.tab.hook.ClanHook
import dev.slne.surf.social.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.social.core.client.tab.redis.redisLoader
import dev.slne.surf.social.core.client.tab.service.TablistService
import dev.slne.surf.social.paper.PaperFeature
import dev.slne.surf.social.paper.tab.command.surfTabCommand
import dev.slne.surf.social.paper.tab.hook.registerLuckPermsListeners
import dev.slne.surf.social.paper.tab.listener.PlayerListener
import dev.slne.surf.social.paper.tab.listener.PlaytimeListener
import dev.slne.surf.social.paper.tab.listener.VanishListener
import dev.slne.surf.social.paper.tab.platform.PaperTabPlatform

object PaperTabFeature : PaperFeature {
    override val name = "tab"

    override suspend fun onLoad() {
        redisLoader.register()
    }

    override suspend fun onEnable() {
        surfTabCommand()

        TablistService.start(PaperTabPlatform())
        registerLuckPermsListeners()
        PlayerListener.register()

        if (isPlaytimeHook) {
            PlaytimeListener.register()
        }

        // Clans are bundled in the same plugin and enabled before tab.
        ClanHook.createListeners()

        if (isContentCreatorHook) {
            ContentCreatorHook.registerListener()
        }
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
val isContentCreatorHook get() = pluginManager.isPluginEnabled("surf-content-creator-paper")
