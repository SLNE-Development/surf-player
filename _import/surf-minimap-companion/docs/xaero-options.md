# Enforceable Xaero options

Every option below can go into `xaero.yml`, keyed by its ID. Enforced options override the
player's own setting, as on a server with Xaero's mods installed. Integers are the positions of Xaero's sliders and
buttons, as Xaero writes them to its own config files.

Taken from Xaero's Minimap 26.6.2, Xaero's World Map 1.47.2 and Xaero's Lib 1.8.5 for Minecraft 26.3.

## Minimap (`minimap`)

The minimap's `entity_radar_categories` and `info_display_config` are not supported.

| Option | Accepted values | Xaero default | In Xaero's settings |
| --- | --- | --- | --- |
| `display_minimap` | `true`, `false` | `true` | Minimap |
| `minimap_item` | text | `-` | Minimap Item |
| `minimap_safe_mode` | `true`, `false` | `false` | Safe Mode |
| `minimap_ui_scale` | 1 to 11 | `1` | UI Scale |
| `display_radar` | `true`, `false` | `true` | Display Radar |
| `radar_dots_style` | 0 to 1 | `0` | Entity Dots Style |
| `radar_smooth_dots` | `true`, `false` | `true` | Smooth Dots |
| `radar_name_scale` | number above 0 | `1.0` | Dot Name Scale |
| `radar_main_entity` | 0 to 2 | `0` | Main Entity As |
| `radar_main_dot_size` | 1 to 4 | `2` | Main Entity Dot Size |
| `radar_hide_invisible_entities` | `true`, `false` | `true` | Hide Invisible Entities |
| `minimap_arrow_scale` | number, 0 or above | `1.5` | Arrow Scale |
| `minimap_arrow_color` | -1 to 7 | `0` | Arrow Color |
| `minimap_arrow_opacity` | 1 to 100 | `100` | Arrow Opacity |
| `tracked_players_on_minimap` | `true`, `false` | `true` | On-Map Tracked Players |
| `tracked_players_in_world` | `true`, `false` | `true` | In-World Tracked Players |
| `tracked_player_minimap_icon_scale` | 0 to 17 | `0` | Tracked Player On-Map Scale |
| `tracked_player_world_icon_scale` | 0 to 17 | `0` | Tracked Player In-World Scale |
| `tracked_player_world_name_scale` | 0 to 17 | `0` | Tracked Player Name Scale |
| `waypoints_in_world` | `true`, `false` | `true` | In-World Waypoints |
| `waypoints_on_minimap` | `true`, `false` | `true` | On-Map Waypoints |
| `waypoints_all_sets` | `true`, `false` | `false` | Render All Waypoint Sets |
| `deathpoints` | `true`, `false` | `true` | Deathpoints |
| `old_deathpoints` | `true`, `false` | `true` | Keep Old Deathpoints |
| `delete_reached_deathpoints` | `true`, `false` | `true` | Delete Reached Deathpoints |
| `switch_auto_waypoints_on_death` | `true`, `false` | `true` | Auto Sub-World On Death |
| `waypoint_icon_scale_in_world` | 0 to 17 | `0` | In-World Waypoint Icon Scale |
| `waypoint_name_scale_in_world` | 0 to 17 | `0` | Waypoint Name Text Scale |
| `waypoint_close_scale_in_world` | number above 0 | `1.0` | In-World Waypoint Up-Close Scale |
| `waypoint_distance_scale_in_world` | 0 to 17 | `0` | Waypoint Distance Text Scale |
| `multiple_waypoints_info` | 0 to 2 | `1` | Multiple Waypoint Distances |
| `waypoint_distance_in_world` | 0 to 2 | `1` | Distance to WP |
| `waypoint_distance_in_menu` | `true`, `false` | `true` | Waypoint Distance In Menu |
| `waypoint_name_in_world` | `true`, `false` | `true` | WP Name Above Distance |
| `waypoint_short_distance_in_world` | `true`, `false` | `false` | Always Disp. Dist. to WP |
| `waypoint_vertical_pointing_angle` | 1 to 180 | `180` | WP Dist. Vertic. Angle |
| `waypoint_horizontal_pointing_angle` | 1 to 180 | `10` | WP Dist. Horis. Angle |
| `waypoint_max_distance` | 0 or above | `0` | Max WP Render Dist. |
| `waypoint_max_distance_dimension_scale` | `true`, `false` | `true` | Dimension-scaled Max WP Distance |
| `waypoint_min_distance_in_world` | 0.0 to 100.0 | `0.0` | Min WP Render Dist. |
| `waypoint_convert_distance_to_km_at` | -1 (never), 0 (always), 1, 10, 100, … 1000000000 | `10000` | Convert Distance to KM at |
| `waypoint_distance_precision` | 0 to 10 | `1` | Waypoint Distance Precision |
| `waypoint_opacity_in_world` | 10 to 100 | `80` | Waypoint Opacity in World |
| `waypoint_opacity_on_minimap` | 10 to 100 | `90` | Waypoint Opacity on Map |
| `waypoint_icon_scale_on_minimap` | 0 to 17 | `0` | On-Map Waypoint Scale |
| `temporary_waypoints_global` | `true`, `false` | `true` | Global Temporary Waypoints |
| `default_waypoint_teleport_format` | text | `/tp @s {x} {y} {z}` | Default Teleport Command |
| `default_waypoint_teleport_rotation_format` | text | `/tp @s {x} {y} {z} {yaw} ~` | Default Teleport Rotation Command |
| `waypoint_teleport_cross_dimension` | `true`, `false` | `true` | Cross-Dimensional TP |
| `waypoint_partial_y_teleport` | `true`, `false` | `true` | Use Y With Partial When TP |
| `new_waypoints_to_bottom` | `true`, `false` | `false` | New Waypoints to Bottom |
| `hide_world_names` | 0 to 2 | `1` | Hide IPs/World Names |
| `minimap_compass_over_everything` | `true`, `false` | `true` | Compass Over Everything |
| `hide_waypoint_coordinates` | `true`, `false` | `false` | Hide Waypoint Coordinates |
| `minimap_compass_location` | 0 to 2 | `1` | Compass |
| `minimap_compass_shadow_color` | 0 to 15 | `9` | Compass Shadow Color |
| `minimap_compass_north_shadow_color` | -1 to 15 | `-1` | North Compass Color |
| `minimap_compass_scale` | 0 to 17 | `0` | Compass Scale |
| `minimap_block_colors` | 0 to 1 | `0` | Block Color Mode |
| `minimap_biomes_in_vanilla_colors` | `true`, `false` | `false` | Biomes in Vanilla Color Mode |
| `minimap_cave_mode_allowed` | `true`, `false` | `true` | Cave Mode |
| `minimap_auto_cave_mode` | 0 to 3 | `2` | Auto Cave Mode |
| `minimap_cave_mode_allowed_dimensions` | list of dimension keys | empty | Cave Mode Dimensions |
| `minimap_manual_cave_mode_start` | any integer | auto | Manual Cave Mode Start Y |
| `minimap_cave_mode_depth` | 0 to 64 | `30` | Cave Mode Depth |
| `minimap_legible_cave_maps` | `true`, `false` | `false` | Legible Cave Maps |
| `minimap_cave_mode_toggle_timer` | 0.0 to 10000.0 | `1000.0` | Cave Mode Toggle Timer |
| `minimap_biome_blending` | `true`, `false` | `true` | Biome Blending |
| `minimap_display_world_map_chunks` | `true`, `false` | `true` | Use World Map Mod Chunks |
| `minimap_terrain_depth` | `true`, `false` | `true` | Terrain Depth |
| `minimap_terrain_slopes` | 0 to 3 | `2` | Terrain Slopes |
| `minimap_display_redstone` | `true`, `false` | `true` | Show Redstone |
| `minimap_display_flowers` | `true`, `false` | `true` | Show Flowers |
| `minimap_block_transparency` | `true`, `false` | `true` | Block Transparency |
| `minimap_display_stained_glass` | `true`, `false` | `true` | Display Stained Glass |
| `minimap_adjust_height_for_short_blocks` | `true`, `false` | `true` | Adjust Y For Short Blocks |
| `minimap_anti_aliasing` | `true`, `false` | `true` | Anti-Aliasing |
| `minimap_size` | 0 (auto), 55 to 250 | `0` | Size |
| `minimap_shape` | 0 to 1 | `0` | Shape |
| `minimap_north_locked` | `true`, `false` | `false` | Lock Minimap North |
| `minimap_zoom` | 1 to 6 | `1` | Zoom |
| `minimap_further_zoomout` | `true`, `false` | `false` | Further Zoom-out |
| `minimap_cave_mode_extra_zoom` | 1 to 4 | `2` | Cave Mode Zoom In |
| `minimap_enlarged_zoom` | 0 to 6 | `0` | Zoom Level When Enlarged |
| `minimap_centered_when_enlarged` | `true`, `false` | `false` | Center When Enlarged |
| `keep_enlarged_minimap_unlocked` | `true`, `false` | `false` | Keep Rotation When Enlarged |
| `minimap_toggled_enlarged` | `true`, `false` | `false` | Toggled Enlarged Minimap |
| `minimap_lighting` | `true`, `false` | `true` | Lighting |
| `minimap_opacity` | 30 to 100 | `100` | Opacity |
| `minimap_frame` | 0 to 3 | `0` | Frame Style |
| `minimap_frame_color` | 0 to 15 | `9` | Frame Color |
| `hide_minimap_under_screen` | `true`, `false` | `true` | Hide When Under Screen |
| `hide_minimap_under_f3` | `true`, `false` | `true` | Hide When Under F3 |
| `boss_health_push_box` | 0 to 2 | `1` | Boss Health Push Box |
| `potion_effect_push_box` | 0 to 2 | `1` | Potion Effects Push Box |
| `minimap_chunk_grid` | -16 to 15 | `-1` | Chunk Grid |
| `minimap_chunk_grid_line_width` | 1 to 8 | `1` | Chunk Grid Line Width |
| `minimap_slime_chunks` | `true`, `false` | `false` | Slime Chunks |
| `minimap_open_slime_chunks_screen` | `true`, `false` | `true` | Open on Key Press |
| `minimap_light_overlay_type` | -3 to 3 | `0` | Light Overlay |
| `minimap_light_overlay_max_light` | 0 to 15 | `7` | Max Light |
| `minimap_light_overlay_min_light` | 0 to 15 | `0` | Min Light |
| `minimap_light_overlay_color` | 0 to 15 | `13` | Color |
| `minimap_display_opac_claims` | `true`, `false` | `true` | Display Chunk Claims |
| `minimap_opac_claim_fill_opacity` | 1 to 100 | `46` | Chunk Claims Fill Opacity |
| `minimap_opac_claim_border_opacity` | 1 to 100 | `80` | Chunk Claims Border Opacity |
| `minimap_display_opac_current_claim` | `true`, `false` | `true` | Display Current Claim |
| `minimap_info_display_background_opacity` | 1 to 100 | `40` | Info Display Background Opacity |
| `minimap_info_display_alignment` | 0 to 2 | `0` | Info Text Alignment |
| `hud_render_layer` | 0 to 100 | `1` | Overlay Render Layer |
| `minimap_undiscovered_opacity` | 0 to 100 | `50` | Undiscovered Opacity |
| `discovered_waystone_waypoints` | `true`, `false` | `true` | Discovered Waystone Waypoints |
| `other_waystone_waypoints` | `true`, `false` | `true` | Other Waystone Waypoints |

## World map (`world-map`)

| Option | Accepted values | Xaero default | In Xaero's settings |
| --- | --- | --- | --- |
| `lighting` | `true`, `false` | `true` | Lighting |
| `block_colors` | 0 to 1 | `0` | Block Colours |
| `load_new_chunks` | `true`, `false` | `true` | Load New Chunks |
| `update_chunks` | `true`, `false` | `true` | Update Chunks |
| `terrain_depth` | `true`, `false` | `true` | Terrain Depth |
| `terrain_slopes` | 0 to 3 | `2` | Terrain Slopes |
| `footsteps` | `true`, `false` | `true` | Footsteps |
| `display_coordinates` | `true`, `false` | `true` | Cursor Coordinates |
| `waypoints` | `true`, `false` | `true` | World Map Waypoints |
| `render_waypoints` | `true`, `false` | `true` | Render World Map Waypoints |
| `waypoint_backgrounds` | `true`, `false` | `true` | Render Waypoint Backgrounds |
| `waypoint_scale` | 0.5 to 5.0 | `1.0` | Waypoint Scale |
| `biome_blending` | `true`, `false` | `true` | Biome Blending |
| `biome_colors_in_vanilla` | `true`, `false` | `false` | Biomes In Vanilla Mode |
| `minimum_zoom_for_local_waypoints` | 0.0 to 3.0 | `0.0` | Min Zoom For Local Waypoints |
| `adjust_height_for_short_blocks` | `true`, `false` | `true` | Adjust Y For Short Blocks |
| `display_flowers` | `true`, `false` | `true` | Load Flowers |
| `display_stained_glass` | `true`, `false` | `true` | Display Stained Glass |
| `cave_mode_allowed` | `true`, `false` | `true` | Full Screen Cave Mode |
| `cave_mode_allowed_dimensions` | list of dimension keys | empty | Full Screen Cave Mode Dimensions |
| `cave_mode_depth` | 1 to 64 | `30` | Cave Mode Depth |
| `legible_cave_maps` | `true`, `false` | `false` | Legible Cave Maps |
| `auto_cave_mode` | -1 to 3 | `-1` | Auto Cave Mode |
| `cave_mode_toggle_timer` | any number | `1.0` | Cave Mode Toggle Timer |
| `default_cave_mode_type` | 0 to 2 | `1` | Default Cave Mode Type |
| `display_cave_mode_start` | `true`, `false` | `true` | Display Cave Mode Top Y |
| `map_writing_distance` | -1 to 32 | `-1` | Map Writing Distance |
| `display_player_as_arrow` | `true`, `false` | `true` | Render Player Arrow |
| `opening_animation` | `true`, `false` | `true` | Animation On Open |
| `arrow_color` | -2 to 7 | `-2` | Arrow Color |
| `display_zoom` | `true`, `false` | `true` | Display Zoom Level |
| `display_hovered_biome` | `true`, `false` | `true` | Cursor Biome |
| `zoom_buttons` | `true`, `false` | `true` | Zoom Buttons |
| `detect_ambiguous_y` | `true`, `false` | `true` | Detect Ambiguous Y In Discovered |
| `default_map_teleport_command_format` | text | `/tp @s {x} {y} {z}` | Default Map Teleport Command |
| `default_map_teleport_command_dimension_format` | text | `/execute as @s in {d} run tp {x} {y} {z}` | Default Cross-Dim Teleport Command |
| `default_player_teleport_command_format` | text | `/tp @s {name}` | Default Player Teleport Command |
| `map_teleport_allowed` | `true`, `false` | `true` | Map Teleportation |
| `partial_y_teleport` | `true`, `false` | `true` | Use Y With Partial When TP |
| `display_opac_claims` | `true`, `false` | `true` | Display Chunk Claims |
| `opac_claims_border_opacity` | 1 to 100 | `80` | Chunk Claims Border Opacity |
| `opac_claims_fill_opacity` | 1 to 100 | `46` | Chunk Claims Fill Opacity |
| `map_item` | text | `-` | Fullscreen Map Item |
| `display_minimap_radar` | `true`, `false` | `true` | Minimap Radar |
| `display_tracked_players` | `true`, `false` | `true` | Display Tracked Players |
| `unlimited_zoom_in` | `true`, `false` | `false` | Unlimited Fullscreen Zoom-in |
| `unlimited_zoom_out` | `true`, `false` | `false` | Unlimited Fullscreen Zoom-out |
| `skip_world_render` | `true`, `false` | `true` | Skip World Render |
| `display_server_chunk_radius` | `true`, `false` | `false` | Display Server Chunk Radius |
| `force_fast_map_writing` | `true`, `false` | `false` | Force Fast Writing |
| `display_map_distances` | `true`, `false` | `true` | Display Map Distances |
| `convert_map_distances_to_km_at` | -1 (never), 0 (always), 1, 10, 100, … 1000000000 | `10000` | Convert Map Distance to KM at |
| `map_distance_precision` | 0 to 10 | `1` | Map Distance Precision |
