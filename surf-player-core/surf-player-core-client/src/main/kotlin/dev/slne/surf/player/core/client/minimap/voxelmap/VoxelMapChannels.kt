package dev.slne.surf.player.core.client.minimap.voxelmap

import dev.slne.surf.player.core.client.minimap.platform.CompanionPlayer

/**
 * The plugin channel VoxelMap's server side sends its settings on. The world ID goes through
 * [dev.slne.surf.player.core.client.minimap.world.WorldInfo], which VoxelMap shares with JourneyMap.
 */
object VoxelMapChannels {
    const val SETTINGS = "voxelmap:settings"

    fun isVoxelMap(channel: String) = channel == SETTINGS

    fun hasVoxelMap(player: CompanionPlayer) = player.hasChannel(SETTINGS)
}
