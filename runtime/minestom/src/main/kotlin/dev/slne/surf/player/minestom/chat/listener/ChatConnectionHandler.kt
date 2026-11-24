package dev.slne.surf.player.minestom.chat.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.chat.message.ConnectionMessageRecipients
import dev.slne.surf.player.core.client.chat.message.format.buildConnectionMessage
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.service.IgnoreService
import dev.slne.surf.player.core.client.util.luckPermsPrefix
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import dev.slne.surf.player.core.client.vanish.VanishedPlayers
import dev.slne.surf.player.minestom.command.asViewer
import dev.slne.surf.player.minestom.chat.message.MinestomMessageFormatter
import net.kyori.adventure.text.Component
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.entity.Player

/**
 * Handles the chat side of players joining and leaving the server.
 */
object ChatConnectionHandler {
    fun onJoin(player: Player) {
        MinestomMessageFormatter.invalidateMentionCache()

        val uuid = player.uuid
        ChatPlatform.launchAsync { IgnoreService.loadIgnoreList(uuid) }

        sendConnectionMessage(player, connectionMessage(player, joined = true))

        if (chatConfig.chatMotdConfig.enabled) {
            player.sendText {
                append(miniMessage.deserialize(chatConfig.chatMotdConfig.message))
            }
        }
    }

    fun onQuit(player: Player) {
        MinestomMessageFormatter.invalidateMentionCache()

        val uuid = player.uuid

        sendConnectionMessage(player, connectionMessage(player, joined = false))

        ChatPlatform.launchAsync {
            IgnoreService.cleanup(uuid)
        }
    }

    private fun connectionMessage(player: Player, joined: Boolean): Component =
        buildConnectionMessage(player.username, luckPermsPrefix(player.uuid), joined)

    /**
     * Sends the join/quit [message] of [player] to every player on this server who sees it, see
     * [ConnectionMessageRecipients.receives], and to the console unless [player] is vanished.
     */
    private fun sendConnectionMessage(player: Player, message: Component) {
        val configEnabled = chatConfig.connectionMessageConfig.enabled
        val vanished = VanishedPlayers.isVanished(player.uuid)

        for (viewer in ConnectionManager.onlinePlayers) {
            val receives = ConnectionMessageRecipients.receives(
                configEnabled = configEnabled,
                playerVanished = vanished,
                viewerIsPlayer = viewer.uuid == player.uuid,
                viewerAlwaysShow = viewer.hasPermission(ChatPermissions.CONNECTION_MESSAGE_ALWAYS_SHOW),
                viewerSettingEnabled = { SurfSettingsApi.getSettingValue(viewer.uuid, SettingKeys.CONNECTION_MESSAGES) },
                viewerCanSeeVanished = { PlayerVisibility.network.canSee(viewer.asViewer(), player.uuid) },
            )

            if (receives) {
                viewer.sendMessage(message)
            }
        }

        if (configEnabled && !vanished) {
            Audiences.console().sendMessage(message)
        }
    }
}
