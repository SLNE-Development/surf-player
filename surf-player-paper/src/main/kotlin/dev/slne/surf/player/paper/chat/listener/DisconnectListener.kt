package dev.slne.surf.player.paper.chat.listener

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.paper.event.common.connection.PlayerQuitMessageEvent
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.core.common.chat.service.IgnoreService
import dev.slne.surf.player.core.common.chat.service.SpyService
import dev.slne.surf.player.paper.chat.message.MessageFormatter
import dev.slne.surf.player.paper.plugin
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
        val messageEvent = PlayerQuitMessageEvent(player, createQuitMessage(player))

        if (!messageEvent.call()) {
            return
        }

        broadcastConnectionMessage(player, messageEvent.message)
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
