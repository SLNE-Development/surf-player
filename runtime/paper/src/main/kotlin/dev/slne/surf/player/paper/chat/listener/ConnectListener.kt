package dev.slne.surf.player.paper.chat.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.event.common.connection.PlayerJoinMessageEvent
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.core.client.chat.message.ConnectionMessageMode
import dev.slne.surf.player.core.client.chat.service.IgnoreService
import dev.slne.surf.player.paper.chat.hook.MiniPlaceholdersHook
import dev.slne.surf.player.paper.chat.message.MessageFormatter
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object ConnectListener : Listener {
    @EventHandler
    fun onAsyncPlayerConnectionConfigure(event: AsyncPlayerConnectionConfigureEvent) {
        runBlocking {
            val uuid = event.connection.profile.id ?: error("Player has no UUID")
            IgnoreService.loadIgnoreList(uuid)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        MessageFormatter.invalidateMentionCache()

        val player = event.player

        // As in the original surf-chat: a message another plugin removed stays removed, and the
        // surf event, our message and the MOTD only follow an announcement (enabled, not vanished,
        // event not cancelled). broadcastConnectionMessage decides who sees our message.
        when (connectionMessageMode(player, event.joinMessage())) {
            ConnectionMessageMode.LEAVE_UNTOUCHED -> return
            ConnectionMessageMode.SUPPRESS -> event.joinMessage(null)

            ConnectionMessageMode.ANNOUNCE -> {
                event.joinMessage(null)

                val messageEvent = PlayerJoinMessageEvent(player, createJoinMessage(player))

                if (!messageEvent.call()) {
                    return
                }

                broadcastConnectionMessage(player, messageEvent.message)
                sendChatMotd(player)
            }
        }
    }

    private fun sendChatMotd(player: Player) {
        if (!chatConfig.chatMotdConfig.enabled) {
            return
        }

        player.sendText {
            append(MiniPlaceholdersHook.parse(player, chatConfig.chatMotdConfig.message))
        }
    }
}
