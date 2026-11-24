package dev.slne.surf.social.paper.vanish.listener

import dev.slne.surf.social.paper.vanish.core.service.vanishService
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

object AdvancementListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (vanishService.isVanished(event.player)) {
            event.message(null)
        }
    }
}