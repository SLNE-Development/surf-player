package dev.slne.surf.minimapcompanion.paper.tracker

import dev.slne.surf.vanish.api.SurfVanishApi
import org.bukkit.entity.Player

/**
 * Asks surf-vanish whether a player is vanished. Only load it when surf-vanish-paper is enabled.
 */
object VanishCheck {
    fun isVanished(player: Player) = SurfVanishApi.isVanished(player)
}
