package dev.slne.surf.player.paper.vanish.listener

import dev.slne.surf.player.paper.vanish.service.VanishService
import dev.slne.surf.player.paper.vanish.util.VanishPermissionRegistry
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent

object GameModeChangeListener : Listener {
    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (event.newGameMode == GameMode.SURVIVAL || event.newGameMode == GameMode.ADVENTURE) {
            if (!event.player.hasPermission(VanishPermissionRegistry.VANISH_SAVE_FLY_STATE)) {
                return
            }

            if (VanishService.getFlyState(event.player.uniqueId)) {
                event.player.allowFlight = true
                event.player.isFlying = true
            }
        } else {
            if (!event.player.hasPermission(VanishPermissionRegistry.VANISH_SAVE_FLY_STATE)) {
                return
            }

            VanishService.setFlyState(event.player.uniqueId, event.player.isFlying)
        }
    }
}