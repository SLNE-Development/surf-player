# Map features by mod

Which option turns a map feature off in each supported mod, for the versions the option lists name:
[xaero-options.md](xaero-options.md), [journeymap-options.md](journeymap-options.md) and
[voxelmap-options.md](voxelmap-options.md). A dash means the mod has no option for it that the plugin can enforce.

The `global-settings` column names the entry in `config.yml` that switches the feature in every mod at once. An entry
set to `true` or `false` overrides the mod files; `per-mod`, the default, leaves the feature to them.

## In all three mods

| Feature | `global-settings` | Xaero (`xaero.yml`) | JourneyMap (`journeymap.yml`) | VoxelMap (`voxelmap.yml`) |
| --- | --- | --- | --- | --- |
| Every map off, for events | - | `disable-maps` in `config.yml` | `disable-maps` in `config.yml` | `disable-maps` in `config.yml` |
| Minimap | `minimap` | `minimap.display_minimap: false` | `minimapEnabled: false` | `minimapAllowed: false` |
| Cave mode | `cave-mode` | `minimap.minimap_cave_mode_allowed: false` and `world-map.cave_mode_allowed: false`; or limited to dimensions with the `…_allowed_dimensions` lists | `caveMapping: NONE` | `cavesAllowed: false` |
| Radar | `radar` | `minimap.display_radar: false` | `radarEnabled: NONE` | `radarAllowed: false` |
| Waypoints | `waypoints` | only hidden: `minimap.waypoints_in_world: false`, `minimap.waypoints_on_minimap: false` and `world-map.waypoints: false`; players can still create them | `allowWaypoints: false` | `waypointsAllowed: false` |
| Death waypoints | `death-waypoints` | `minimap.deathpoints: false` | `allowDeathPoints: false`, which also deletes the existing ones | `deathWaypointAllowed: false`; existing ones stay as normal waypoints |

## In two of them

| Feature | `global-settings` | Xaero (`xaero.yml`) | JourneyMap (`journeymap.yml`) | VoxelMap (`voxelmap.yml`) |
| --- | --- | --- | --- | --- |
| Teleporting from the map | `teleport` | `world-map.map_teleport_allowed: false` | `teleport: NONE` | - (only `teleportCommand`, which sets the command) |
| Players on the radar | `radar-players` | - | `playerRadarEnabled: false` | `radarPlayersAllowed: false` |
| Mobs on the radar | `radar-mobs` (in JourneyMap also animals and villagers) | - | `mobRadarEnabled: false` (animals and villagers have their own options) | `radarMobsAllowed: false` (hostile and neutral mobs) |
| Coordinates | `coordinates` | `world-map.display_coordinates: false` (cursor on the world map), `minimap.hide_waypoint_coordinates: true` | `hideCoordinates: true` | - |
| Players tracked by the server | - | tracker in `config.yml` | tracker in `config.yml` | - |

## In one of them

| Feature | Mod | Option |
| --- | --- | --- |
| World map alone | VoxelMap | `worldmapAllowed: false` |
| Surface, topographic and biome maps | JourneyMap | `surfaceMapping`, `topoMapping`, `biomeMapping` |
| Render distance | JourneyMap | `surfaceRenderRange`, `caveRenderRange` |
| Radar range and entity counts | JourneyMap | `radarLateralDistance`, `radarVerticalDistance`, `max…Data` |
| Global waypoints only | JourneyMap | `globalWaypointsOnly` |

Xaero's entity radar categories, which would hold the players and mobs on its radar, cannot be enforced by the plugin.
