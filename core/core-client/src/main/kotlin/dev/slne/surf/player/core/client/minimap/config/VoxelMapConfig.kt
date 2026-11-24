package dev.slne.surf.player.core.client.minimap.config

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

/**
 * What `voxelmap.yml` holds: the VoxelMap settings this server enforces.
 */
@ConfigSerializable
data class VoxelMapConfig(
    @param:Comment("Settings this server enforces on VoxelMap, by VoxelMap's key (for example cavesAllowed: false). docs/voxelmap-options.md lists them all.")
    val options: Map<String, ConfigurationNode> = emptyMap(),
)
