package dev.slne.surf.player.minestom.minimap.listener

import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.journeymap.JourneyMapChannels
import dev.slne.surf.player.core.client.minimap.service.MapSyncService
import dev.slne.surf.player.core.client.minimap.xaero.XaeroSyncService
import dev.slne.surf.player.core.client.minimap.tracker.PlayerTracker
import dev.slne.surf.player.core.client.minimap.voxelmap.VoxelMapChannels
import dev.slne.surf.player.core.client.minimap.world.WorldInfo
import dev.slne.surf.player.core.client.minimap.xaero.XaeroChannels
import dev.slne.surf.player.core.client.minimap.xaero.XaeroMapEffects
import dev.slne.surf.player.minestom.minimap.platform.MinestomMinimapPlayer
import dev.slne.surf.player.minestom.minimap.platform.RegisteredChannels
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerPluginMessageEvent
import net.minestom.server.event.player.PlayerRespawnEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.timer.TaskSchedule

/**
 * Syncs players whenever their client could have lost or not yet received what the map mods need.
 */
object CompanionListener : EventRegistrar {
    // Minestom does not track players itself; it only clears what other servers sent.
    private val tracker = PlayerTracker()

    override fun register(node: EventNode<Event>) {
        // Also fires during the configuration phase, before the player has an instance.
        node.addListener<PlayerPluginMessageEvent> { event ->
            val player = event.player

            if (event.identifier != ChannelAnnounceListener.REGISTER_CHANNEL) {
                // JourneyMap's requests for its permissions, and the world ID JourneyMap and VoxelMap ask for.
                if (event.identifier != WorldInfo.CHANNEL && !JourneyMapChannels.isJourneyMap(event.identifier)) return@addListener
                val instance = player.instance ?: return@addListener
                MapSyncService.answer(MinestomMinimapPlayer(player, instance), event.identifier)
                return@addListener
            }

            val channels = RegisteredChannels.parseRegisterPayload(event.message)
            RegisteredChannels.register(player.uuid, channels)

            val instance = player.instance ?: return@addListener
            val modChannels = channels.filter {
                XaeroChannels.isXaero(it) || JourneyMapChannels.isJourneyMap(it) || VoxelMapChannels.isVoxelMap(it)
            }
            if (modChannels.isNotEmpty()) {
                val companionPlayer = MinestomMinimapPlayer(player, instance)
                if (modChannels.any(XaeroChannels::isMap)) tracker.reset(companionPlayer)
                MapSyncService.syncChannels(companionPlayer, modChannels)
            }
        }

        // Fires on the first spawn and on every instance change; the first spawn continues in onJoin.
        node.addListener<PlayerSpawnEvent> { event ->
            MapSyncService.sync(MinestomMinimapPlayer(event.player, event.instance))
        }

        // The client drops every effect on respawn, and a respawn in the same instance fires no spawn event.
        node.addListener<PlayerRespawnEvent> { event ->
            val player = event.player
            val instance = player.instance ?: return@addListener
            XaeroMapEffects.sync(MinestomMinimapPlayer(player, instance), CompanionConfigs.current.config.disableMaps)
        }
    }

    /**
     * Called on [player]'s first spawn, after the spawn listener synced them.
     */
    fun onJoin(player: Player) {
        val instance = player.instance ?: return
        tracker.reset(MinestomMinimapPlayer(player, instance))

        for (delay in XaeroSyncService.JOIN_RESEND_DELAYS) {
            player.scheduler().buildTask {
                val currentInstance = player.instance ?: return@buildTask
                MapSyncService.sync(MinestomMinimapPlayer(player, currentInstance))
            }.delay(TaskSchedule.millis(delay.inWholeMilliseconds)).schedule()
        }
    }

    fun onQuit(player: Player) {
        val uuid = player.uuid
        RegisteredChannels.forget(uuid)
        MapSyncService.forget(uuid)
        tracker.forget(uuid)
    }
}
