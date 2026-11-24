package dev.slne.surf.player.minestom.tab

import dev.slne.surf.api.core.event.SurfEventBus
import dev.slne.surf.player.core.client.tab.hook.ClanHook
import dev.slne.surf.player.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.player.core.client.tab.hook.LuckPermsHook
import dev.slne.surf.player.core.client.tab.listener.AfkListener
import dev.slne.surf.player.core.client.tab.redis.TabRedisLoader
import dev.slne.surf.player.core.client.tab.service.TablistService
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.player.minestom.command.tabAdminActions
import dev.slne.surf.player.minestom.tab.platform.MinestomTabPlayer
import net.minestom.server.entity.Player

object TabMinestomFeature : MinestomPlayerFeature {
    override val id = "tab"

    override suspend fun onLoad() {
        TabRedisLoader.register()

        TablistService.start()

        subscribeToLuckPermsUserChanges(LuckPermsHook::updatePlayerInTablist)
        ClanHook.createListeners()
        ContentCreatorHook.registerListener()
    }

    override suspend fun onEnable() {
        SurfEventBus.registerListeners(AfkListener)
    }

    override fun adminActions() = tabAdminActions()

    override suspend fun onDisable() {
        SurfEventBus.unregisterListeners(AfkListener)
        TablistService.stop()
    }

    override fun onPlayerJoin(player: Player) {
        TablistService.viewerJoined(MinestomTabPlayer(player))
    }

    override fun onPlayerQuit(player: Player) {
        TablistService.viewerLeft(player.uuid)
    }
}
