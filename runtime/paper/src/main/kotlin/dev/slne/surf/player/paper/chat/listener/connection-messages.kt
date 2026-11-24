package dev.slne.surf.player.paper.chat.listener

import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.core.client.chat.message.ConnectionMessageHandling
import dev.slne.surf.player.core.client.chat.message.ConnectionMessageMode
import dev.slne.surf.player.core.client.chat.message.ConnectionMessageRecipients
import dev.slne.surf.player.core.client.chat.message.format.buildConnectionMessage
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.util.luckPermsPrefix
import dev.slne.surf.player.core.client.vanish.VanishedPlayers
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun createJoinMessage(player: Player) =
    buildConnectionMessage(player.name, luckPermsPrefix(player.uniqueId), joined = true)

fun createQuitMessage(player: Player) =
    buildConnectionMessage(player.name, luckPermsPrefix(player.uniqueId), joined = false)

/**
 * How the join/quit listener treats [player]'s vanilla message [vanillaMessage], see
 * [ConnectionMessageHandling.mode].
 */
fun connectionMessageMode(player: Player, vanillaMessage: Component?): ConnectionMessageMode =
    ConnectionMessageHandling.mode(
        vanillaMessagePresent = vanillaMessage != null,
        configEnabled = chatConfig.connectionMessageConfig.enabled,
        playerVanished = VanishedPlayers.isVanished(player.uniqueId),
    )

/**
 * Sends the announced join/quit [message] of [player] to the console and to every player on this
 * server who sees it, see [ConnectionMessageRecipients.receives].
 */
fun broadcastConnectionMessage(player: Player, message: Component) {
    val alwaysShow = player.hasPermission(ChatPermissions.CONNECTION_MESSAGE_ALWAYS_SHOW)

    forEachPlayer { viewer ->
        val receives = ConnectionMessageRecipients.receives(alwaysShow) {
            SurfSettingsApi.getSettingValue(viewer.uniqueId, SettingKeys.CONNECTION_MESSAGES)
        }

        if (receives) {
            viewer.sendMessage(message)
        }
    }

    Bukkit.getConsoleSender().sendMessage(message)
}
