package dev.slne.surf.social.minestom.chat.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.social.core.client.chat.config.chatConfig
import dev.slne.surf.social.core.client.chat.hook.LuckPermsHook
import dev.slne.surf.social.core.client.chat.hook.SettingsHook
import dev.slne.surf.social.core.client.chat.message.format.buildConnectionMessage
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.client.chat.platform.ChatPlatform
import dev.slne.surf.social.core.common.chat.service.IgnoreService
import dev.slne.surf.social.core.common.chat.service.SpyService
import dev.slne.surf.social.minestom.chat.message.MinestomMessageFormatter
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent

/**
 * Handles the chat side of players joining and leaving the server.
 */
object ConnectionListener : EventRegistrar {
    override fun register(node: EventNode<Event>) {
        node.addListener<PlayerSpawnEvent> { event ->
            if (event.isFirstSpawn) {
                onJoin(event.player)
            }
        }

        node.addListener<PlayerDisconnectEvent> { event ->
            onQuit(event.player)
        }
    }

    private fun onJoin(player: Player) {
        MinestomMessageFormatter.invalidateMentionCache()

        val uuid = player.uuid
        ChatPlatform.launchAsync { IgnoreService.loadIgnoreList(uuid) }

        if (chatConfig.connectionMessageConfig.enabled) {
            sendConnectionMessage(player, joined = true)
        }

        if (chatConfig.chatMotdConfig.enabled) {
            player.sendText {
                append(miniMessage.deserialize(chatConfig.chatMotdConfig.message))
            }
        }
    }

    private fun onQuit(player: Player) {
        MinestomMessageFormatter.invalidateMentionCache()

        val uuid = player.uuid

        if (chatConfig.connectionMessageConfig.enabled) {
            sendConnectionMessage(player, joined = false)
        }

        ChatPlatform.launchAsync {
            IgnoreService.cleanup(uuid)
            SpyService.cleanup(uuid)
        }
    }

    private fun sendConnectionMessage(player: Player, joined: Boolean) {
        val message = buildConnectionMessage(
            player.username,
            LuckPermsHook.getPrefix(player.uuid),
            joined = joined
        )

        val alwaysShow =
            player.hasPermission(ChatPermissions.CONNECTION_MESSAGE_ALWAYS_SHOW)

        if (alwaysShow) {
            ChatPlatform.broadcast(message)
        } else {
            ConnectionManager.onlinePlayers.forEach { viewer ->
                if (SettingsHook.hasConnectionMessagesEnabled(viewer.uuid)) {
                    viewer.sendMessage(message)
                }
            }
        }
    }
}
