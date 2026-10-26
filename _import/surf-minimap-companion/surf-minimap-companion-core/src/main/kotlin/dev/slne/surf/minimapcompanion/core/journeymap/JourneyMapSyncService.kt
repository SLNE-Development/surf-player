package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.config.JourneyMapConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.id.LevelIds
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer

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

        sendWorldId(player, config)
        val permissions = JourneyMapPermissions.json(JourneyMapProfile.of(journeyMap), config.disableMaps, playerRadar)
        player.sendPluginMessage(JourneyMapChannels.PERMISSIONS, JourneyMapPackets.permissions(permissions))
    }

    /**
     * Answers a request the client sent on [channel]. Only permission and world ID requests get an answer;
     * the version handshake never does, so the client cannot disconnect itself over our version.
     */
    fun answer(player: CompanionPlayer, channel: String, config: MinimapCompanionConfig, journeyMap: JourneyMapConfig, playerRadar: Boolean) {
        if (!JourneyMapChannels.hasJourneyMap(player)) return

        when (channel) {
            JourneyMapChannels.PERMISSIONS -> sync(player, config, journeyMap, playerRadar)
            JourneyMapChannels.WORLD_ID -> sendWorldId(player, config)
        }
    }

    // The same ID Xaero gets for the world, so server-id and world-ids apply to JourneyMap too.
    private fun sendWorldId(player: CompanionPlayer, config: MinimapCompanionConfig) {
        val levelId = LevelIds.resolve(player.worldKey, player.worldIdentity, config)
        player.sendPluginMessage(JourneyMapChannels.WORLD_ID, JourneyMapPackets.worldId(levelId.toString()))
    }
}
