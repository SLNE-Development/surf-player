package dev.slne.surf.player.paper.tab.listener

import dev.slne.surf.player.core.client.tab.service.TablistService
import dev.slne.surf.player.paper.tab.platform.PaperTabPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerShowEntityEvent

object PlayerListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        TablistService.viewerJoined(PaperTabPlayer(event.player))
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        TablistService.viewerLeft(event.player.uniqueId)
    }

    @EventHandler
    fun onShow(event: PlayerShowEntityEvent) {
        val target = event.entity as? Player ?: return

        TablistService.updateEntry(target.uniqueId)
    }
}
