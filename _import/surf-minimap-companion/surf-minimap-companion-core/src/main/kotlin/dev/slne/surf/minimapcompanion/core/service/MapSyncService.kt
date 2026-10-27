package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.config.*
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapChannels
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapSyncService
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlatform
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapChannels
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapSyncService
import dev.slne.surf.minimapcompanion.core.world.WorldInfo
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroSyncService
import java.util.*

/**
 * Syncs every map mod the companion supports. The platforms call it wherever a client could have lost or not
 * yet received what the mods need; each mod only reaches clients that have it.
 */
object MapSyncService {

    /**
     * Sends [player] everything each map mod needs for their current world. JourneyMap shows only the players the
     * server sends while a tracker switch is on and the platform [tracksPlayers].
     */
    fun sync(
        player: CompanionPlayer,
        config: MinimapCompanionConfig = CompanionConfig.getConfig(),
        xaero: XaeroConfig = CompanionXaeroConfig.getConfig(),
        journeyMap: JourneyMapConfig = CompanionJourneyMapConfig.getConfig(),
        voxelMap: VoxelMapConfig = CompanionVoxelMapConfig.getConfig(),
        tracksPlayers: Boolean = CompanionPlatform.tracksPlayers,
    ) {
        XaeroSyncService.sync(player, XaeroChannels.ALL, config, xaero)
        JourneyMapSyncService.sync(player, config, journeyMap, playerRadar(config, tracksPlayers))
        VoxelMapSyncService.sync(player, config, voxelMap)
    }

    /**
     * Syncs the mods whose [channels] the client just registered.
     */
    fun syncChannels(
        player: CompanionPlayer,
        channels: Collection<String>,
        config: MinimapCompanionConfig = CompanionConfig.getConfig(),
        xaero: XaeroConfig = CompanionXaeroConfig.getConfig(),
        journeyMap: JourneyMapConfig = CompanionJourneyMapConfig.getConfig(),
        voxelMap: VoxelMapConfig = CompanionVoxelMapConfig.getConfig(),
        tracksPlayers: Boolean = CompanionPlatform.tracksPlayers,
    ) {
        val xaeroChannels = channels.filter(XaeroChannels::isXaero)
        if (xaeroChannels.isNotEmpty()) {
            XaeroSyncService.sync(player, xaeroChannels, config, xaero)
        }
        if (JourneyMapChannels.PERMISSIONS in channels) {
            JourneyMapSyncService.sync(player, config, journeyMap, playerRadar(config, tracksPlayers))
        }
        if (VoxelMapChannels.SETTINGS in channels) {
            VoxelMapSyncService.sync(player, config, voxelMap)
        }
    }

    /**
     * Answers a request the client sent on [channel]. World ID requests are answered for every client that asks,
     * whichever mod sent them.
     */
    fun answer(
        player: CompanionPlayer,
        channel: String,
        config: MinimapCompanionConfig = CompanionConfig.getConfig(),
        journeyMap: JourneyMapConfig = CompanionJourneyMapConfig.getConfig(),
        tracksPlayers: Boolean = CompanionPlatform.tracksPlayers,
    ) {
        if (channel == WorldInfo.CHANNEL) {
            WorldInfo.send(player, config)
            return
        }
        JourneyMapSyncService.answer(player, channel, config, journeyMap, playerRadar(config, tracksPlayers))
    }

    fun forget(playerUuid: UUID) {
        XaeroSyncService.forget(playerUuid)
    }

    private fun playerRadar(config: MinimapCompanionConfig, tracksPlayers: Boolean) = tracksPlayers && config.tracker.isEnabled
}
