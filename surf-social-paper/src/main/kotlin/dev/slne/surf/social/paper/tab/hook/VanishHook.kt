package dev.slne.surf.social.paper.tab.hook

import dev.slne.surf.social.api.vanish.SurfVanishApi
import org.bukkit.Bukkit
import java.util.*

object VanishHook {
    fun isVanished(playerUuid: UUID) =
        Bukkit.getPlayer(playerUuid)?.let { SurfVanishApi.isVanished(it) } ?: false
}