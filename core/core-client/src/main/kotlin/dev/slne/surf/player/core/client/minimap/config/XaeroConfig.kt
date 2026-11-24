package dev.slne.surf.player.core.client.minimap.config

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

/**
 * The Xaero settings this server enforces, as a server with Xaero's mods installed would. Clients cannot change
 * enforced settings; settings left out stay theirs.
 */
@ConfigSerializable
data class XaeroConfig(
    @param:Comment("Options enforced on Xaero's Minimap, by Xaero's option ID (for example display_radar: false). docs/minimap/xaero-options.md in the surf-player repository lists them all.")
    val minimap: Map<String, ConfigurationNode> = emptyMap(),

    @param:Comment("Options enforced on Xaero's World Map, by Xaero's option ID (for example cave_mode_allowed: false). docs/minimap/xaero-options.md in the surf-player repository lists them all.")
    val worldMap: Map<String, ConfigurationNode> = emptyMap(),
)
