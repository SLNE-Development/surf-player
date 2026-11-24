package dev.slne.surf.player.paper.vanish.listener

import dev.slne.surf.player.paper.vanish.util.isVanished
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

object AdvancementListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (event.player.isVanished) {
            event.message(null)
        }
    }
}