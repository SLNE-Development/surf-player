package dev.slne.surf.social.paper.nametag.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.social.core.client.nametag.service.NametagService
import dev.slne.surf.social.paper.nametag.platform.PaperNametagPlayer
import dev.slne.surf.social.paper.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerHideEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerShowEntityEvent
object NametagListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = PaperNametagPlayer(event.player)

        plugin.launch {
            NametagService.handleJoin(player)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = PaperNametagPlayer(event.player)

        plugin.launch {
            NametagService.handleLeave(player)
        }
    }

    @EventHandler
    fun onHide(event: PlayerHideEntityEvent) {
        val hiddenPlayer = event.entity as? Player ?: return
        val hidden = PaperNametagPlayer(hiddenPlayer)
        val viewer = PaperNametagPlayer(event.player)

        plugin.launch {
            NametagService.handleLeave(hidden, viewer)
        }
    }

    @EventHandler
    fun onShow(event: PlayerShowEntityEvent) {
        val shownPlayer = event.entity as? Player ?: return
        val shown = PaperNametagPlayer(shownPlayer)
        val viewer = PaperNametagPlayer(event.player)

        plugin.launch {
            NametagService.handleJoin(shown, viewer)
        }
    }
}
