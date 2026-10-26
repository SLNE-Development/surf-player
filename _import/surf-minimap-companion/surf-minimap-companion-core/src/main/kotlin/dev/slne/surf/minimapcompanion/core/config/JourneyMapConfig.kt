package dev.slne.surf.minimapcompanion.core.config

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

/**
 * What `journeymap.yml` holds: the JourneyMap settings this server enforces.
 */
@ConfigSerializable
data class JourneyMapConfig(
    @param:Comment("Settings this server enforces on JourneyMap, by JourneyMap's field name (for example caveMapping: NONE). journeymap-options.md lists them all.")
    val options: Map<String, ConfigurationNode> = emptyMap(),
)
