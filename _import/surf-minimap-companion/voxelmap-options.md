# Enforceable VoxelMap options

Every option below can go under `options` in `voxelmap.yml`, keyed by VoxelMap's own name. Enforced options apply
as on a server with VoxelMap's server part installed. Options left out keep VoxelMap's default, which allows
everything.

Taken from VoxelMap Updated 1.16.13 for Minecraft 26.3.

The plugin sends every option on every sync, with VoxelMap's default for the ones left out, so a player coming from
another server through the proxy does not keep that server's restrictions. While `disable-maps` in `config.yml` is
on, every `true`/`false` option is sent as `false`.

`serverIdentity` and `worldName` are set by the plugin and cannot be set here: `serverIdentity` would move the
player's map data on their computer, and the world name comes from the world ID the plugin sends for each world.
Options VoxelMap does not know are skipped with a warning, since VoxelMap ignores them.

| Option | Accepted values | VoxelMap default | Effect |
| --- | --- | --- | --- |
| `radarAllowed` | `true`, `false` | `true` | Radar; `false` empties it completely |
| `radarMobsAllowed` | `true`, `false` | `true` | Mobs, hostile and neutral, on the radar |
| `radarPlayersAllowed` | `true`, `false` | `true` | Players on the radar |
| `cavesAllowed` | `true`, `false` | `true` | Cave mode of the minimap |
| `minimapAllowed` | `true`, `false` | `true` | Minimap; `false` neither renders nor shows it |
| `worldmapAllowed` | `true`, `false` | `true` | World map; `false` records no new chunks and draws nothing. The screen still opens and says "The Worldmap is disabled on this server." |
| `waypointsAllowed` | `true`, `false` | `true` | Waypoints; `false` hides them everywhere and locks the waypoint menu |
| `deathWaypointAllowed` | `true`, `false` | `true` | Death waypoint; `false` creates none on death, existing ones stay as normal waypoints |
| `teleportCommand` | text | the client's own `tp %p %x %y %z` | Command for teleporting from the world map, the waypoint list and `ztp`, with the placeholders `%p` (player), `%x`, `%y` and `%z`. A leading `/` is removed, since VoxelMap sends the command without one. VoxelMap has no switch that forbids teleporting |
