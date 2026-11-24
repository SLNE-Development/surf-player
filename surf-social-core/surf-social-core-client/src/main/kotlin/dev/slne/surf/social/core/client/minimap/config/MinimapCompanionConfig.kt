package dev.slne.surf.social.core.client.minimap.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

/**
 * What `config.yml` holds: the settings that apply to every map mod the companion supports.
 * Settings of a single mod live in its own file, such as `xaero.yml`.
 */
@ConfigSerializable
data class MinimapCompanionConfig(
    @param:Comment("Turns every supported map mod (Xaero's mods, JourneyMap and VoxelMap) off completely, for events such as a maze where players must not see the map. To restrict single map features, use global-settings for every mod at once, or the mod's own file, such as xaero.yml, journeymap.yml or voxelmap.yml.")
    val disableMaps: Boolean = false,

    @param:Comment("Switches a map feature for every supported mod at once. per-mod leaves it to xaero.yml, journeymap.yml and voxelmap.yml; true allows it and false forbids it in every mod that has it, whatever the mod files say. disable-maps still turns everything off. docs/map-features.md lists what each entry changes in each mod.")
    val globalSettings: GlobalSettingsConfig = GlobalSettingsConfig(),

    @param:Comment("Mixed into every computed level ID. Set it only when servers share worlds copied from one template, since copies keep the same world UUID.")
    val serverId: String = "",

    @param:Comment("Fixed level IDs by world key, as Xaero shows it (for example \"minecraft:overworld\"). An entry here replaces the computed ID, for Xaero's mods and in the world ID JourneyMap and VoxelMap get.")
    val worldIds: Map<String, Int> = emptyMap(),

    @param:Comment("Which other players each player sees on Xaero's maps and JourneyMap beyond their view distance. Only players on this server are shown.")
    val tracker: TrackerConfig = TrackerConfig(),
)
