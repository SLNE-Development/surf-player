package dev.slne.surf.social.paper.chat.listener

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.paper.event.common.connection.PlayerQuitMessageEvent
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.social.core.client.chat.config.chatConfig
import dev.slne.surf.social.core.client.chat.hook.LuckPermsHook
import dev.slne.surf.social.core.client.chat.message.format.buildConnectionMessage
import dev.slne.surf.social.core.common.chat.service.IgnoreService
import dev.slne.surf.social.core.common.chat.service.SpyService
import dev.slne.surf.social.core.client.chat.hook.SettingsHook
import dev.slne.surf.social.paper.chat.message.MessageFormatter
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry
import dev.slne.surf.social.paper.chat.PaperChatFeature
import dev.slne.surf.social.paper.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object DisconnectListener : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onDisconnect(event: PlayerQuitEvent) {
        MessageFormatter.invalidateMentionCache()

        if (event.quitMessage() == null) {
            return
        }

        event.quitMessage(null)

        if (!chatConfig.connectionMessageConfig.enabled) {
            return
        }

        val player = event.player
        val alwaysShow = player.hasPermission(PermissionRegistry.CONNECTION_MESSAGE_ALWAYS_SHOW)
        var message = buildConnectionMessage(
            player.name,
            LuckPermsHook.getPrefix(player.uniqueId),
            joined = false
        )

        val messageEvent = PlayerQuitMessageEvent(player, message)

        if (!messageEvent.call()) {
            return
        }

        message = messageEvent.message

        if (alwaysShow) {
            server.broadcast(message)
        } else {
            val settingsHook = PaperChatFeature.checkSettingsHook()

            forEachPlayer { player ->
                if (!settingsHook || SettingsHook.hasConnectionMessagesEnabled(player.uniqueId)) {
                    player.sendMessage(message)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerConnectionClose(event: PlayerConnectionCloseEvent) {
        plugin.launch {
            val uuid = event.playerUniqueId
            IgnoreService.cleanup(uuid)
            SpyService.cleanup(uuid)
        }
    }
}