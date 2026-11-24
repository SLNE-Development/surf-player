package dev.slne.surf.player.paper.minimap.hook

import dev.slne.surf.player.api.vanish.SurfVanishApi
import org.bukkit.entity.Player

/**
 * Asks surf-vanish whether a player is vanished. Only load it when surf-vanish-paper is enabled.
 */
object VanishHook {
    fun isVanished(player: Player) = SurfVanishApi.isVanished(player)
}
