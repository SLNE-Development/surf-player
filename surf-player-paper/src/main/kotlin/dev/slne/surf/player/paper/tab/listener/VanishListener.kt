package dev.slne.surf.player.paper.tab.listener

import dev.slne.surf.player.core.client.tab.service.TablistService
import dev.slne.surf.player.api.vanish.event.PlayerNickEvent
import dev.slne.surf.player.api.vanish.event.PlayerReappearEvent
import dev.slne.surf.player.api.vanish.event.PlayerUnNickEvent
import dev.slne.surf.player.api.vanish.event.PlayerVanishEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object VanishListener : Listener {
    @EventHandler
    fun onVanish(event: PlayerVanishEvent) = updateEntry(event.player)

    @EventHandler
    fun onReappear(event: PlayerReappearEvent) = updateEntry(event.player)

    @EventHandler
    fun onNick(event: PlayerNickEvent) = updateEntry(event.player)

    @EventHandler
    fun onUnNick(event: PlayerUnNickEvent) = updateEntry(event.player)

    private fun updateEntry(player: Player?) {
        if (player == null) return

        TablistService.updateEntry(player.uniqueId)
    }
}
