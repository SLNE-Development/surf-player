package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.minimapcompanion.core.platform.MinimapCompanionPlatform

/**
 * Loads [MinimapCompanionConfig] from `config.yml` in the plugin's data directory.
 */
object CompanionConfig : SpongeYmlConfigClass<MinimapCompanionConfig>(
    MinimapCompanionConfig::class.java,
    MinimapCompanionPlatform.dataPath,
    "config.yml"
)

/**
 * Loads [XaeroConfig] from `xaero.yml` in the plugin's data directory.
 */
object CompanionXaeroConfig : SpongeYmlConfigClass<XaeroConfig>(
    XaeroConfig::class.java,
    MinimapCompanionPlatform.dataPath,
    "xaero.yml"
)

/**
 * Loads [JourneyMapConfig] from `journeymap.yml` in the plugin's data directory.
 */
object CompanionJourneyMapConfig : SpongeYmlConfigClass<JourneyMapConfig>(
    JourneyMapConfig::class.java,
    MinimapCompanionPlatform.dataPath,
    "journeymap.yml"
)
