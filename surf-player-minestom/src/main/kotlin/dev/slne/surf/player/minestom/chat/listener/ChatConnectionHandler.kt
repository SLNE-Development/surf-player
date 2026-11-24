package dev.slne.surf.social.minestom.chat.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.social.core.client.chat.config.chatConfig
import dev.slne.surf.social.core.client.chat.hook.SettingsHook
import dev.slne.surf.social.core.client.chat.message.format.buildConnectionMessage
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.client.chat.platform.ChatPlatform
import dev.slne.surf.social.core.common.chat.service.IgnoreService
import dev.slne.surf.social.core.client.util.luckPermsPrefix
import dev.slne.surf.social.core.common.chat.service.SpyService
import dev.slne.surf.social.minestom.chat.message.MinestomMessageFormatter
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player

/**
 * Handles the chat side of players joining and leaving the server.
 */
object ChatConnectionHandler {
    fun onJoin(player: Player) {
        MinestomMessageFormatter.invalidateMentionCache()

        val uuid = player.uuid
        ChatPlatform.launchAsync { IgnoreService.loadIgnoreList(uuid) }

        if (chatConfig.connectionMessageConfig.enabled) {
            sendConnectionMessage(player, connectionMessage(player, joined = true))
        }

        if (chatConfig.chatMotdConfig.enabled) {
            player.sendText {
                append(miniMessage.deserialize(chatConfig.chatMotdConfig.message))
            }
        }
    }

    fun onQuit(player: Player) {
        MinestomMessageFormatter.invalidateMentionCache()

        val uuid = player.uuid

        if (chatConfig.connectionMessageConfig.enabled) {
            sendConnectionMessage(player, connectionMessage(player, joined = false))
        }

        ChatPlatform.launchAsync {
            IgnoreService.cleanup(uuid)
            SpyService.cleanup(uuid)
        }
    }

    private fun connectionMessage(player: Player, joined: Boolean): Component =
        buildConnectionMessage(player.username, luckPermsPrefix(player.uuid), joined)

    private fun sendConnectionMessage(player: Player, message: Component) {
        if (player.hasPermission(ChatPermissions.CONNECTION_MESSAGE_ALWAYS_SHOW)) {
            ChatPlatform.broadcast(message)
            return
        }

        ConnectionManager.onlinePlayers
            .filter { viewer -> SettingsHook.hasConnectionMessagesEnabled(viewer.uuid) }
            .forEach { viewer -> viewer.sendMessage(message) }
    }
}
