package dev.slne.surf.player.core.client.minimap.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.player.core.client.minimap.platform.CompanionPlatform

/**
 * Loads [MinimapCompanionConfig] from `config.yml` in the plugin's data directory.
 */
object CompanionConfig : SpongeYmlConfigClass<MinimapCompanionConfig>(
    MinimapCompanionConfig::class.java,
    CompanionPlatform.dataPath,
    "config.yml"
)

/**
 * Loads [XaeroConfig] from `xaero.yml` in the plugin's data directory.
 */
object CompanionXaeroConfig : SpongeYmlConfigClass<XaeroConfig>(
    XaeroConfig::class.java,
    CompanionPlatform.dataPath,
    "xaero.yml"
)

/**
 * Loads [JourneyMapConfig] from `journeymap.yml` in the plugin's data directory.
 */
object CompanionJourneyMapConfig : SpongeYmlConfigClass<JourneyMapConfig>(
    JourneyMapConfig::class.java,
    CompanionPlatform.dataPath,
    "journeymap.yml"
)

/**
 * Loads [VoxelMapConfig] from `voxelmap.yml` in the plugin's data directory.
 */
object CompanionVoxelMapConfig : SpongeYmlConfigClass<VoxelMapConfig>(
    VoxelMapConfig::class.java,
    CompanionPlatform.dataPath,
    "voxelmap.yml"
)
