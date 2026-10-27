package dev.slne.surf.minimapcompanion.minestom.listener

import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapChannels
import dev.slne.surf.minimapcompanion.core.service.MapSyncService
import dev.slne.surf.minimapcompanion.core.xaero.XaeroSyncService
import dev.slne.surf.minimapcompanion.core.tracker.PlayerTracker
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapChannels
import dev.slne.surf.minimapcompanion.core.world.WorldInfo
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroMapEffects
import dev.slne.surf.minimapcompanion.minestom.platform.MinestomCompanionPlayer
import dev.slne.surf.minimapcompanion.minestom.platform.RegisteredChannels
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
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
                MapSyncService.answer(MinestomCompanionPlayer(player, instance), event.identifier)
                return@addListener
            }

            val channels = RegisteredChannels.parseRegisterPayload(event.message)
            RegisteredChannels.register(player.uuid, channels)

            val instance = player.instance ?: return@addListener
            val modChannels = channels.filter {
                XaeroChannels.isXaero(it) || JourneyMapChannels.isJourneyMap(it) || VoxelMapChannels.isVoxelMap(it)
            }
            if (modChannels.isNotEmpty()) {
                val companionPlayer = MinestomCompanionPlayer(player, instance)
                if (modChannels.any(XaeroChannels::isMap)) tracker.reset(companionPlayer)
                MapSyncService.syncChannels(companionPlayer, modChannels)
            }
        }

        // Fires on the first spawn and on every instance change.
        node.addListener<PlayerSpawnEvent> { event ->
            val player = event.player
            val companionPlayer = MinestomCompanionPlayer(player, event.instance)

            MapSyncService.sync(companionPlayer)
            if (!event.isFirstSpawn) return@addListener

            tracker.reset(companionPlayer)

            for (delay in XaeroSyncService.JOIN_RESEND_DELAYS) {
                player.scheduler().buildTask {
                    val instance = player.instance ?: return@buildTask
                    MapSyncService.sync(MinestomCompanionPlayer(player, instance))
                }.delay(TaskSchedule.millis(delay.inWholeMilliseconds)).schedule()
            }
        }

        // The client drops every effect on respawn, and a respawn in the same instance fires no spawn event.
        node.addListener<PlayerRespawnEvent> { event ->
            val player = event.player
            val instance = player.instance ?: return@addListener
            XaeroMapEffects.sync(MinestomCompanionPlayer(player, instance), CompanionConfigs.current.config.disableMaps)
        }

        node.addListener<PlayerDisconnectEvent> { event ->
            val uuid = event.player.uuid
            RegisteredChannels.forget(uuid)
            MapSyncService.forget(uuid)
            tracker.forget(uuid)
        }
    }
}
