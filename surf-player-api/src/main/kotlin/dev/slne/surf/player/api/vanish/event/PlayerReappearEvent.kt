package dev.slne.surf.player.api.vanish.event

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class PlayerReappearEvent(
    val playerUuid: UUID
) : Event() {
    val player get() = Bukkit.getPlayer(playerUuid)

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
