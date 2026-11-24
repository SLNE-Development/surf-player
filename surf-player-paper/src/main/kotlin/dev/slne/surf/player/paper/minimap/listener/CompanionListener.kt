package dev.slne.surf.player.paper.minimap.listener

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.journeymap.JourneyMapChannels
import dev.slne.surf.player.core.client.minimap.service.MapSyncService
import dev.slne.surf.player.core.client.minimap.xaero.XaeroSyncService
import dev.slne.surf.player.core.client.minimap.voxelmap.VoxelMapChannels
import dev.slne.surf.player.core.client.minimap.xaero.XaeroChannels
import dev.slne.surf.player.core.client.minimap.xaero.XaeroMapEffects
import dev.slne.surf.player.paper.minimap.platform.PaperCompanionPlayer
import dev.slne.surf.player.paper.minimap.plugin
import dev.slne.surf.player.paper.minimap.tracker.PaperTracker
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
        resendMapEffects(player)
    }

    // The client drops every effect on respawn, so the maps would come back.
    @EventHandler
    fun onPostRespawn(event: PlayerPostRespawnEvent) {
        XaeroMapEffects.sync(PaperCompanionPlayer(event.player), CompanionConfigs.current.config.disableMaps)
        resendMapEffects(event.player)
    }

    // PacketEvents writes the effects straight to the connection, past packets Paper may still hold back, such as
    // chunks Anti-Xray is still working on. An effect that overtakes the respawn packet is dropped by the client,
    // so the effects go out once more on the next tick.
    private fun resendMapEffects(player: Player) {
        player.scheduler.run(plugin, {
            XaeroMapEffects.sync(PaperCompanionPlayer(player), CompanionConfigs.current.config.disableMaps)
        }, null)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        MapSyncService.forget(event.player.uniqueId)
        PaperTracker.forget(event.player)
    }
}
