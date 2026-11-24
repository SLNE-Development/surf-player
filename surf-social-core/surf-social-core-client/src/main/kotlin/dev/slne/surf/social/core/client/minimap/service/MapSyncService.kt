package dev.slne.surf.social.core.client.minimap.service

import dev.slne.surf.social.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.social.core.client.minimap.config.CompanionSettings
import dev.slne.surf.social.core.client.minimap.journeymap.JourneyMapChannels
import dev.slne.surf.social.core.client.minimap.journeymap.JourneyMapSyncService
import dev.slne.surf.social.core.client.minimap.platform.CompanionPlayer
import dev.slne.surf.social.core.client.minimap.platform.CompanionPlatform
import dev.slne.surf.social.core.client.minimap.voxelmap.VoxelMapChannels
import dev.slne.surf.social.core.client.minimap.voxelmap.VoxelMapSyncService
import dev.slne.surf.social.core.client.minimap.world.WorldInfo
import dev.slne.surf.social.core.client.minimap.xaero.XaeroChannels
import dev.slne.surf.social.core.client.minimap.xaero.XaeroSyncService
import java.util.*

/**
 * Syncs every map mod the companion supports. The platforms call it wherever a client could have lost or not
 * yet received what the mods need; each mod only reaches clients that have it. Each call reads one snapshot of the
 * config files, [CompanionConfigs.current] unless the caller passes one.
 */
object MapSyncService {

    /**
     * Sends [player] everything each map mod needs for their current world. JourneyMap shows only the players the
     * server sends while a tracker switch is on and the platform [tracksPlayers].
     */
    fun sync(
        player: CompanionPlayer,
        settings: CompanionSettings = CompanionConfigs.current,
        tracksPlayers: Boolean = CompanionPlatform.tracksPlayers,
    ) {
        XaeroSyncService.sync(player, XaeroChannels.ALL, settings.config, settings.xaero)
        JourneyMapSyncService.sync(player, settings.config, settings.journeyMap, playerRadar(settings, tracksPlayers))
        VoxelMapSyncService.sync(player, settings.config, settings.voxelMap)
    }

    /**
     * Syncs the mods whose [channels] the client just registered.
     */
    fun syncChannels(
        player: CompanionPlayer,
        channels: Collection<String>,
        settings: CompanionSettings = CompanionConfigs.current,
        tracksPlayers: Boolean = CompanionPlatform.tracksPlayers,
    ) {
        val xaeroChannels = channels.filter(XaeroChannels::isXaero)
        if (xaeroChannels.isNotEmpty()) {
            XaeroSyncService.sync(player, xaeroChannels, settings.config, settings.xaero)
        }
        if (JourneyMapChannels.PERMISSIONS in channels) {
            JourneyMapSyncService.sync(player, settings.config, settings.journeyMap, playerRadar(settings, tracksPlayers))
        }
        if (VoxelMapChannels.SETTINGS in channels) {
            VoxelMapSyncService.sync(player, settings.config, settings.voxelMap)
        }
    }

    /**
     * Answers a request the client sent on [channel]. World ID requests are answered for every client that asks,
     * whichever mod sent them.
     */
    fun answer(
        player: CompanionPlayer,
        channel: String,
        settings: CompanionSettings = CompanionConfigs.current,
        tracksPlayers: Boolean = CompanionPlatform.tracksPlayers,
    ) {
        if (channel == WorldInfo.CHANNEL) {
            WorldInfo.send(player, settings.config)
            return
        }
        JourneyMapSyncService.answer(player, channel, settings.config, settings.journeyMap, playerRadar(settings, tracksPlayers))
    }

    fun forget(playerUuid: UUID) {
        XaeroSyncService.forget(playerUuid)
    }

    private fun playerRadar(settings: CompanionSettings, tracksPlayers: Boolean) = tracksPlayers && settings.config.tracker.isEnabled
}
