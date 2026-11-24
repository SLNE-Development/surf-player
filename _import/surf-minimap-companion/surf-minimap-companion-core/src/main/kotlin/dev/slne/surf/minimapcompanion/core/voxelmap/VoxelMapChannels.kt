package dev.slne.surf.minimapcompanion.core.voxelmap

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer

/**
 * The plugin channel VoxelMap's server side sends its settings on. The world ID goes through
 * [dev.slne.surf.minimapcompanion.core.world.WorldInfo], which VoxelMap shares with JourneyMap.
 */
object VoxelMapChannels {
    const val SETTINGS = "voxelmap:settings"

    fun isVoxelMap(channel: String) = channel == SETTINGS

    fun hasVoxelMap(player: CompanionPlayer) = player.hasChannel(SETTINGS)
}
