package dev.slne.surf.minimapcompanion.core.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

/**
 * What `config.yml` holds: the settings that apply to every map mod the companion supports.
 * Settings of a single mod live in its own file, such as `xaero.yml`.
 */
@ConfigSerializable
data class MinimapCompanionConfig(
    @param:Comment("Turns every supported map mod off completely, for events such as a maze where players must not see the map. To restrict single map features, use the mod's own file, such as xaero.yml.")
    val disableMaps: Boolean = false,

    @param:Comment("Mixed into every computed level ID. Set it only when servers share worlds copied from one template, since copies keep the same world UUID.")
    val serverId: String = "",

    @param:Comment("Fixed level IDs by world key, as Xaero shows it (for example \"minecraft:overworld\"). An entry here replaces the computed ID.")
    val worldIds: Map<String, Int> = emptyMap(),

    @param:Comment("Which other players each player sees on the maps beyond their view distance. Only players on this server are shown.")
    val tracker: TrackerConfig = TrackerConfig(),
)
