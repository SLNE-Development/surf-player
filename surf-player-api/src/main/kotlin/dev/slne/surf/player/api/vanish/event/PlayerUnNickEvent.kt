package dev.slne.surf.social.api.vanish.event

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class PlayerUnNickEvent(
    val playerUuid: UUID
) : Event() {
    val player get() = Bukkit.getPlayer(playerUuid)

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
