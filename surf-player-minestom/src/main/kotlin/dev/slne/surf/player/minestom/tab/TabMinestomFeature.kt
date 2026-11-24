package dev.slne.surf.player.minestom.tab

import dev.slne.surf.player.core.client.tab.hook.ClanHook
import dev.slne.surf.player.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.player.core.client.tab.hook.LuckPermsHook
import dev.slne.surf.player.core.client.tab.redis.redisLoader
import dev.slne.surf.player.core.client.tab.service.TablistService
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.PlayerMinestomPlugin
import dev.slne.surf.player.minestom.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.player.minestom.tab.command.surfTabCommand
import dev.slne.surf.player.minestom.tab.listener.PlaytimeListener
import dev.slne.surf.player.minestom.tab.platform.MinestomTabPlatform
import dev.slne.surf.player.minestom.tab.platform.MinestomTabPlayer
import net.minestom.server.entity.Player

object TabMinestomFeature : MinestomPlayerFeature {
    override val id = "tab"

    override suspend fun onLoad() {
        redisLoader.register()

        TablistService.start(MinestomTabPlatform())

        subscribeToLuckPermsUserChanges(LuckPermsHook::updatePlayerInTablist)
        ClanHook.createListeners()
        ContentCreatorHook.registerListener()
    }

    override suspend fun onEnable() {
        surfTabCommand()

        PlaytimeListener.register(PlayerMinestomPlugin.eventNode)
    }

    override suspend fun onDisable() {
        TablistService.stop()
    }

    override fun onPlayerJoin(player: Player) {
        TablistService.viewerJoined(MinestomTabPlayer(player))
    }

    override fun onPlayerQuit(player: Player) {
        TablistService.viewerLeft(player.uuid)
    }
}
