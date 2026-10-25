package dev.slne.surf.minimapcompanion.paper.listener

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import dev.slne.surf.minimapcompanion.core.service.XaeroSyncService
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent

/**
 * Syncs players whenever their client could have lost or not yet received the Xaero data.
 */
object CompanionListener : Listener {

    @EventHandler
    fun onRegisterChannel(event: PlayerRegisterChannelEvent) {
        val channel = event.channel
        if (!XaeroChannels.isXaero(channel)) return

        // The event can fire off the player's region thread, so read the world on it.
        val player = event.player
        player.scheduler.run(plugin, { XaeroSyncService.sync(PaperCompanionPlayer(player), listOf(channel)) }, null)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        XaeroSyncService.sync(PaperCompanionPlayer(player))

        for (delay in XaeroSyncService.JOIN_RESEND_DELAYS) {
            val ticks = delay.inWholeMilliseconds / 50
            player.scheduler.runDelayed(plugin, { XaeroSyncService.sync(PaperCompanionPlayer(player)) }, null, ticks)
        }
    }

    // Fires on Folia too, and only once the client has switched to the new world.
    @EventHandler
    fun onAddToWorld(event: EntityAddToWorldEvent) {
        val player = event.entity as? Player ?: return
        XaeroSyncService.sync(PaperCompanionPlayer(player, event.world))
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        XaeroSyncService.forget(event.player.uniqueId)
    }
}
