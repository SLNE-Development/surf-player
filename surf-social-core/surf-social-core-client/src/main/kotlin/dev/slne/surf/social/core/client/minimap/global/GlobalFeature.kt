package dev.slne.surf.minimapcompanion.core.global

/**
 * A map feature that more than one supported mod has, switched for all of them by its [key] under
 * `global-settings` in `config.yml`.
 */
enum class GlobalFeature(val key: String) {
    MINIMAP("minimap"),
    CAVE_MODE("cave-mode"),
    RADAR("radar"),
    WAYPOINTS("waypoints"),
    DEATH_WAYPOINTS("death-waypoints"),
    TELEPORT("teleport"),
    RADAR_PLAYERS("radar-players"),
    RADAR_MOBS("radar-mobs"),
    COORDINATES("coordinates"),
}
