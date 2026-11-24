package dev.slne.surf.player.paper.playtime.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.client.playtime.session.handlePlaytimeJoin
import dev.slne.surf.player.core.client.playtime.session.startPlaytimeSession
import dev.slne.surf.player.paper.playtime.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object PlayerJoinListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        startPlaytimeSession(event.player.uniqueId)

        plugin.launch {
            handlePlaytimeJoin(event.player.uniqueId, event.player)
        }
    }
}
