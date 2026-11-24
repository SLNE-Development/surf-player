package dev.slne.surf.player.paper.chat.util

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.player.core.client.chat.message.format.appendName
import dev.slne.surf.player.core.client.util.luckPermsPrefix
import org.bukkit.entity.Player

fun SurfComponentBuilder.appendName(player: Player, allowTeleport: Boolean) =
    appendName(player.name, player.uniqueId, luckPermsPrefix(player.uniqueId), allowTeleport)
