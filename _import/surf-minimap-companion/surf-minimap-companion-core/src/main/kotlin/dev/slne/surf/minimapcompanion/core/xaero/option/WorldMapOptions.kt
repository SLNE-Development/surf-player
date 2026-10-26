package dev.slne.surf.minimapcompanion.core.xaero.option

import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.bool
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.dimensions
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.double
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.indexed
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.int
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.string

/**
 * The options a server can enforce on Xaero's World Map, as Xaero's World Map 1.47.2 defines them.
 */
object WorldMapOptions : XaeroOptionCatalog(
    bool("lighting"),
    int("block_colors", 0..1),
    bool("load_new_chunks"),
    bool("update_chunks"),
    bool("terrain_depth"),
    int("terrain_slopes", 0..3),
    bool("footsteps"),
    bool("display_coordinates"),
    bool("waypoints"),
    bool("render_waypoints"),
    bool("waypoint_backgrounds"),
    double("waypoint_scale", 0.5..5.0),
    bool("biome_blending"),
    bool("biome_colors_in_vanilla"),
    double("minimum_zoom_for_local_waypoints", 0.0..3.0),
    bool("adjust_height_for_short_blocks"),
    bool("display_flowers"),
    bool("display_stained_glass"),
    bool("cave_mode_allowed"),
    dimensions("cave_mode_allowed_dimensions"),
    int("cave_mode_depth", 1..64),
    bool("legible_cave_maps"),
    int("auto_cave_mode", -1..3),
    double("cave_mode_toggle_timer"),
    int("default_cave_mode_type", 0..2),
    bool("display_cave_mode_start"),
    int("map_writing_distance", -1..32),
    bool("display_player_as_arrow"),
    bool("opening_animation"),
    int("arrow_color", -2..7),
    bool("display_zoom"),
    bool("display_hovered_biome"),
    bool("zoom_buttons"),
    bool("detect_ambiguous_y"),
    string("default_map_teleport_command_format"),
    string("default_map_teleport_command_dimension_format"),
    string("default_player_teleport_command_format"),
    bool("map_teleport_allowed"),
    bool("partial_y_teleport"),
    bool("display_opac_claims"),
    int("opac_claims_border_opacity", 1..100),
    int("opac_claims_fill_opacity", 1..100),
    string("map_item"),
    bool("display_minimap_radar"),
    bool("display_tracked_players"),
    bool("unlimited_zoom_in"),
    bool("unlimited_zoom_out"),
    bool("skip_world_render"),
    bool("display_server_chunk_radius"),
    bool("force_fast_map_writing"),
    bool("display_map_distances"),
    indexed("convert_map_distances_to_km_at", -1..10, ::kilometreThreshold),
    int("map_distance_precision", 0..10),
)
