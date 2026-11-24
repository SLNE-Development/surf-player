package dev.slne.surf.player.core.client.minimap.voxelmap

import dev.slne.surf.player.core.client.minimap.config.MinimapCompanionConfig
import dev.slne.surf.player.core.client.minimap.config.VoxelMapConfig
import dev.slne.surf.player.core.client.minimap.global.GlobalSettings
import dev.slne.surf.player.core.client.minimap.platform.CompanionPlayer

/**
 * Sends VoxelMap clients the settings VoxelMap's server part sends. The world ID is only sent when the client
 * asks for it, through [dev.slne.surf.player.core.client.minimap.world.WorldInfo].
 */
object VoxelMapSyncService {

    /**
     * Sends [player] the settings this server enforces, if their client has VoxelMap.
     */
    fun sync(player: CompanionPlayer, config: MinimapCompanionConfig, voxelMap: VoxelMapConfig) {
        if (!VoxelMapChannels.hasVoxelMap(player)) return

        val profile = VoxelMapProfile.of(voxelMap, GlobalSettings.of(config.globalSettings))
        val json = VoxelMapSettings.json(profile, config.disableMaps)
        player.sendPluginMessage(VoxelMapChannels.SETTINGS, VoxelMapPackets.settings(json))
    }
}
