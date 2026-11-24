package dev.slne.surf.social.paper.chat.util

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.social.core.client.chat.hook.LuckPermsHook
import dev.slne.surf.social.core.client.chat.message.format.appendName
import org.bukkit.entity.Player

fun SurfComponentBuilder.appendName(player: Player, allowTeleport: Boolean) =
    appendName(player.name, player.uniqueId, LuckPermsHook.getPrefix(player.uniqueId), allowTeleport)
