package dev.slne.surf.minimapcompanion.paper.listener

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapChannels
import dev.slne.surf.minimapcompanion.core.service.MapSyncService
import dev.slne.surf.minimapcompanion.core.service.XaeroSyncService
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroMapEffects
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.plugin
import dev.slne.surf.minimapcompanion.paper.tracker.PaperTracker
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent

/**
 * Syncs players whenever their client could have lost or not yet received what the map mods need.
 */
object CompanionListener : Listener {

    @EventHandler
    fun onRegisterChannel(event: PlayerRegisterChannelEvent) {
        val channel = event.channel
        if (!XaeroChannels.isXaero(channel) && !JourneyMapChannels.isJourneyMap(channel) && !VoxelMapChannels.isVoxelMap(channel)) {
            return
        }

        // The event can fire off the player's region thread, so read the world on it.
        val player = event.player
        player.scheduler.run(plugin, {
            if (XaeroChannels.isMap(channel)) PaperTracker.reset(player)
            MapSyncService.syncChannels(PaperCompanionPlayer(player), listOf(channel))
        }, null)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        MapSyncService.sync(PaperCompanionPlayer(player))
        PaperTracker.reset(player)
        PaperTracker.track(player)

        for (delay in XaeroSyncService.JOIN_RESEND_DELAYS) {
            val ticks = delay.inWholeMilliseconds / 50
            player.scheduler.runDelayed(plugin, { MapSyncService.sync(PaperCompanionPlayer(player)) }, null, ticks)
        }
    }

    // Fires on Folia too, and only once the client has switched to the new world.
    @EventHandler
    fun onAddToWorld(event: EntityAddToWorldEvent) {
        val player = event.entity as? Player ?: return
        MapSyncService.sync(PaperCompanionPlayer(player, event.world))
    }

    // The client drops every effect on respawn, so the maps would come back.
    @EventHandler
    fun onPostRespawn(event: PlayerPostRespawnEvent) {
        XaeroMapEffects.sync(PaperCompanionPlayer(event.player), CompanionConfig.getConfig().disableMaps)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        MapSyncService.forget(event.player.uniqueId)
        PaperTracker.forget(event.player)
    }
}
