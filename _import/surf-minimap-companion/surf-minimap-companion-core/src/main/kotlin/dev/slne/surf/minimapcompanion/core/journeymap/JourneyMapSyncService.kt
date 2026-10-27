package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.config.JourneyMapConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.core.worldinfo.WorldInfo

/**
 * Sends JourneyMap clients what JourneyMap's server addon sends: the world ID, then the permissions.
 */
object JourneyMapSyncService {

    /**
     * Sends [player] the world ID of their current world and the permissions, if their client has JourneyMap.
     * [playerRadar] makes JourneyMap show only the players the server sends.
     */
    fun sync(player: CompanionPlayer, config: MinimapCompanionConfig, journeyMap: JourneyMapConfig, playerRadar: Boolean) {
        if (!JourneyMapChannels.hasJourneyMap(player)) return

        WorldInfo.send(player, config)
        val profile = JourneyMapProfile.of(journeyMap, GlobalSettings.of(config.globalSettings))
        val permissions = JourneyMapPermissions.json(profile, config.disableMaps, playerRadar)
        player.sendPluginMessage(JourneyMapChannels.PERMISSIONS, JourneyMapPackets.permissions(permissions))
    }

    /**
     * Answers a request the client sent on [channel]. Only permission requests get an answer here; world ID
     * requests are answered for every mod by [WorldInfo]. The version handshake is never answered, so the
     * client cannot disconnect itself over our version.
     */
    fun answer(player: CompanionPlayer, channel: String, config: MinimapCompanionConfig, journeyMap: JourneyMapConfig, playerRadar: Boolean) {
        if (!JourneyMapChannels.hasJourneyMap(player)) return

        if (channel == JourneyMapChannels.PERMISSIONS) {
            sync(player, config, journeyMap, playerRadar)
        }
    }
}
