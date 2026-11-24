package dev.slne.surf.social.paper.chat.util

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable

fun Cancellable.cancel() {
    isCancelled = true
}

fun PacketWrapper<*>.send(player: Player) =
    PacketEvents.getAPI().playerManager.sendPacket(player, this)
