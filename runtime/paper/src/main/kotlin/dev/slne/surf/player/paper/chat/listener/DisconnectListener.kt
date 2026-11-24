package dev.slne.surf.player.paper.chat.listener

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.paper.event.common.connection.PlayerQuitMessageEvent
import dev.slne.surf.player.core.client.chat.message.ConnectionMessageMode
import dev.slne.surf.player.core.client.chat.service.IgnoreService
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

        val player = event.player

        // Same rules as on join, see ConnectListener.
        when (connectionMessageMode(player, event.quitMessage())) {
            ConnectionMessageMode.LEAVE_UNTOUCHED -> return
            ConnectionMessageMode.SUPPRESS -> event.quitMessage(null)

            ConnectionMessageMode.ANNOUNCE -> {
                event.quitMessage(null)

                val messageEvent = PlayerQuitMessageEvent(player, createQuitMessage(player))

                if (!messageEvent.call()) {
                    return
                }

                broadcastConnectionMessage(player, messageEvent.message)
            }
        }
    }

    @EventHandler
    fun onPlayerConnectionClose(event: PlayerConnectionCloseEvent) {
        plugin.launch {
            val uuid = event.playerUniqueId
            IgnoreService.cleanup(uuid)
            // Spying is network-wide and ends on a network quit, see ChatRedisListener.
        }
    }
}
