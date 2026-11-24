package dev.slne.surf.player.paper.settings.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.common.settings.service.SettingsService
import dev.slne.surf.player.paper.settings.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerConnectionListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.launch {
            SettingsService.cachePlayerSettings(player.uniqueId)

            if (!player.isOnline) {
                SettingsService.invalidatePlayerSettingsCache(player.uniqueId)
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        SettingsService.invalidatePlayerSettingsCache(event.player.uniqueId)
    }
}
