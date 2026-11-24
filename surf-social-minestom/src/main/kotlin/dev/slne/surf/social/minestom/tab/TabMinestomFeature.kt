package dev.slne.surf.social.minestom.tab

import dev.slne.surf.social.core.client.tab.hook.ClanHook
import dev.slne.surf.social.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.social.core.client.tab.hook.LuckPermsHook
import dev.slne.surf.social.core.client.tab.redis.redisLoader
import dev.slne.surf.social.core.client.tab.service.TablistService
import dev.slne.surf.social.minestom.MinestomSocialFeature
import dev.slne.surf.social.minestom.SocialMinestomPlugin
import dev.slne.surf.social.minestom.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.social.minestom.tab.command.surfTabCommand
import dev.slne.surf.social.minestom.tab.listener.PlaytimeListener
import dev.slne.surf.social.minestom.tab.platform.MinestomTabPlatform
import dev.slne.surf.social.minestom.tab.platform.MinestomTabPlayer
import net.minestom.server.entity.Player

object TabMinestomFeature : MinestomSocialFeature {
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

        PlaytimeListener.register(SocialMinestomPlugin.eventNode)
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
