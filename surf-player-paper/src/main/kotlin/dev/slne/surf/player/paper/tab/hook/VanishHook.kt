package dev.slne.surf.player.paper.tab.hook

import dev.slne.surf.player.api.vanish.SurfVanishApi
import org.bukkit.Bukkit
import java.util.*

object VanishHook {
    fun isVanished(playerUuid: UUID) =
        Bukkit.getPlayer(playerUuid)?.let { SurfVanishApi.isVanished(it) } ?: false
}