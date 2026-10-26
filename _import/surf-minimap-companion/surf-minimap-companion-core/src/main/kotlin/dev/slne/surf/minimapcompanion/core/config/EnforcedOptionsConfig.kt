package dev.slne.surf.minimapcompanion.core.config

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class EnforcedOptionsConfig(
    @param:Comment("Options enforced on Xaero's Minimap, by Xaero's option ID (for example display_radar: false). xaero-options.md lists them all.")
    val minimap: Map<String, ConfigurationNode> = emptyMap(),

    @param:Comment("Options enforced on Xaero's World Map, by Xaero's option ID (for example cave_mode_allowed: false). xaero-options.md lists them all.")
    val worldMap: Map<String, ConfigurationNode> = emptyMap(),
)
