# Enforceable JourneyMap options

Every option below can go under `options` in `journeymap.yml`, keyed by JourneyMap's own field name. Enforced
options override what the player may do, as on a server with JourneyMap's server addon installed. Options left out
keep JourneyMap's default.

Taken from JourneyMap 6.0.10 for Minecraft 26.3.

The plugin sets `journeymapEnabled` (from `disable-maps` in `config.yml`) and `worldPlayerRadar` (from the tracker
switches) itself; they cannot be set here. `OPS` is not accepted for the options that take `ALL` or `NONE`, since it
would need settings per player.

| Option | Accepted values | JourneyMap default | Effect |
| --- | --- | --- | --- |
| `minimapEnabled` | `true`, `false` | `true` | Minimap |
| `hideCoordinates` | `true`, `false` | `false` | Hides coordinates |
| `surfaceMapping` | `ALL`, `NONE` | `ALL` | Surface map |
| `topoMapping` | `ALL`, `NONE` | `ALL` | Topographic map |
| `biomeMapping` | `ALL`, `NONE` | `ALL` | Biome map |
| `caveMapping` | `ALL`, `NONE` | `ALL` | Cave map |
| `surfaceRenderRange` | 0 to 32 | `0` | Maximum surface render distance in chunks, `0` for no limit |
| `caveRenderRange` | 0 to 32 | `0` | Maximum cave render distance in chunks, `0` for no limit |
| `radarEnabled` | `ALL`, `NONE` | `ALL` | Radar |
| `playerRadarEnabled` | `true`, `false` | `true` | Players on the radar |
| `playerRadarNamesEnabled` | `true`, `false` | `true` | Player names on the radar |
| `villagerRadarEnabled` | `true`, `false` | `true` | Villagers on the radar |
| `animalRadarEnabled` | `true`, `false` | `true` | Animals on the radar |
| `mobRadarEnabled` | `true`, `false` | `true` | Mobs on the radar |
| `radarLateralDistance` | 16 to 512 | `512` | Horizontal radar range in blocks |
| `radarVerticalDistance` | 8 to 320 | `320` | Vertical radar range in blocks |
| `maxAnimalsData` | 1 to 128 | `128` | Most animals on the radar |
| `maxAmbientCreaturesData` | 1 to 128 | `128` | Most ambient creatures on the radar |
| `maxMobsData` | 1 to 128 | `128` | Most mobs on the radar |
| `maxPlayersData` | 1 to 128 | `128` | Most players on the radar |
| `maxVillagersData` | 1 to 128 | `128` | Most villagers on the radar |
| `allowWaypoints` | `true`, `false` | `true` | Waypoints |
| `allowDeathPoints` | `true`, `false` | `true` | Death points; `false` also deletes the player's existing ones |
| `showInGameBeacons` | `true`, `false` | `true` | Waypoint beacons in the world |
| `globalWaypointsOnly` | `true`, `false` | `false` | Only global waypoints |
| `teleport` | `NONE`, `ALL`, `OPS`, `WAYPOINT_ONLY`, `GLOBAL_ONLY` | `OPS` | Teleporting from the map; `OPS` means nobody, since the plugin never makes a player a JourneyMap admin |
