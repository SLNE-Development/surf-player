package dev.slne.surf.player.paper.playtime.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.client.playtime.afk.AfkTracker
import dev.slne.surf.player.core.client.playtime.session.endPlaytimeSessions
import dev.slne.surf.player.core.client.playtime.session.handlePlaytimeJoin
import dev.slne.surf.player.core.client.playtime.session.invalidatePlaytimeCaches
import dev.slne.surf.player.core.client.playtime.session.startPlaytimeSession
import dev.slne.surf.player.paper.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Opens and closes the playtime session of a player and records their activity for the afk
 * detection.
 */
object PlaytimeListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        startPlaytimeSession(player.uniqueId)

        plugin.launch {
            handlePlaytimeJoin(player.uniqueId, player)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val playerUuid = event.player.uniqueId
        invalidatePlaytimeCaches(playerUuid)

        plugin.launch {
            endPlaytimeSessions(playerUuid)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!event.hasChangedOrientation()) return

        AfkTracker.recordActivity(event.player.uniqueId)
    }
}
