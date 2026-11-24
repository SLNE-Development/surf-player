package dev.slne.surf.player.paper.tab

import dev.slne.surf.api.core.event.SurfEventBus
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.tab.hook.ClanHook
import dev.slne.surf.player.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.player.core.client.tab.hook.LuckPermsHook
import dev.slne.surf.player.core.client.tab.listener.AfkListener
import dev.slne.surf.player.core.client.tab.redis.TabRedisLoader
import dev.slne.surf.player.core.client.tab.service.TablistService
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.player.paper.tab.command.surfTabCommand
import dev.slne.surf.player.paper.tab.listener.PlayerListener
import dev.slne.surf.player.paper.tab.listener.VanishListener

object PaperTabFeature : PaperPlayerFeature {
    override val name = "tab"

    override suspend fun onLoad() {
        TabRedisLoader.register()
    }

    override suspend fun onEnable() {
        surfTabCommand()

        TablistService.start()
        subscribeToLuckPermsUserChanges(LuckPermsHook::updatePlayerInTablist)
        PlayerListener.register()

        SurfEventBus.registerListeners(AfkListener)

        // Clans and content creators are bundled in the same plugin and enabled before tab.
        ClanHook.createListeners()
        ContentCreatorHook.registerListener()

        SurfEventBus.registerListeners(VanishListener)
    }

    override suspend fun onDisable() {
        SurfEventBus.unregisterListeners(VanishListener)
        SurfEventBus.unregisterListeners(AfkListener)
        TablistService.stop()
    }
}
