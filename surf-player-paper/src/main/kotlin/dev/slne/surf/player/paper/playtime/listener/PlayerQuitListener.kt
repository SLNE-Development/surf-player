package dev.slne.surf.player.paper.playtime.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.client.playtime.session.endPlaytimeSessions
import dev.slne.surf.player.core.client.playtime.session.invalidatePlaytimeCaches
import dev.slne.surf.player.paper.playtime.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object PlayerQuitListener : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        invalidatePlaytimeCaches(event.player.uniqueId)

        plugin.launch {
            endPlaytimeSessions(event.player.uniqueId)
        }
    }
}
