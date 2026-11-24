package dev.slne.surf.player.paper.chat.listener

import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.chat.message.format.buildConnectionMessage
import dev.slne.surf.player.core.client.util.luckPermsPrefix
import dev.slne.surf.player.paper.chat.permission.PermissionRegistry
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

fun createJoinMessage(player: Player) =
    buildConnectionMessage(player.name, luckPermsPrefix(player.uniqueId), joined = true)

fun createQuitMessage(player: Player) =
    buildConnectionMessage(player.name, luckPermsPrefix(player.uniqueId), joined = false)

/**
 * Sends the join/quit [message] of [player] to everyone who has connection messages enabled,
 * or to everyone if the player's connection messages must always be shown.
 */
fun broadcastConnectionMessage(player: Player, message: Component) {
    if (player.hasPermission(PermissionRegistry.CONNECTION_MESSAGE_ALWAYS_SHOW)) {
        server.broadcast(message)
        return
    }

    forEachPlayer { viewer ->
        if (SurfSettingsApi.getSettingValue(viewer.uniqueId, SettingKeys.CONNECTION_MESSAGES)) {
            viewer.sendMessage(message)
        }
    }
}
