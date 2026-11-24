package dev.slne.surf.player.core.client.minimap.config

import dev.slne.surf.player.core.client.minimap.global.GlobalSettings
import dev.slne.surf.player.core.client.minimap.journeymap.JourneyMapProfile
import dev.slne.surf.player.core.client.minimap.voxelmap.VoxelMapProfile
import dev.slne.surf.player.core.client.minimap.xaero.XaeroServerProfile

/**
 * The four config files as they were loaded together, with what they enforce on each mod. A sync reads one snapshot,
 * so it never mixes files from before and after a reload.
 */
class CompanionSettings(
    val config: MinimapCompanionConfig = MinimapCompanionConfig(),
    val xaero: XaeroConfig = XaeroConfig(),
    val journeyMap: JourneyMapConfig = JourneyMapConfig(),
    val voxelMap: VoxelMapConfig = VoxelMapConfig(),
) {
    val global = GlobalSettings.of(config.globalSettings)
    val xaeroProfile = XaeroServerProfile.of(xaero, global)
    val journeyMapProfile = JourneyMapProfile.of(journeyMap, global)
    val voxelMapProfile = VoxelMapProfile.of(voxelMap, global)
}
