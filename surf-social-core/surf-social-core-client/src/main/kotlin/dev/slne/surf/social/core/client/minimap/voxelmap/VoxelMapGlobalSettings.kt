package dev.slne.surf.social.core.client.minimap.voxelmap

import dev.slne.surf.social.core.client.minimap.global.GlobalFeature
import dev.slne.surf.social.core.client.minimap.global.GlobalSettings
import dev.slne.surf.social.core.client.minimap.global.OptionOverlay

/**
 * Applies `global-settings` to the options of `voxelmap.yml`. VoxelMap's switches allow a feature when `true`, so
 * each entry sends its own value. VoxelMap has nothing for teleporting or coordinates.
 */
object VoxelMapGlobalSettings {

    private val OPTIONS = mapOf(
        GlobalFeature.MINIMAP to "minimapAllowed",
        GlobalFeature.CAVE_MODE to "cavesAllowed",
        GlobalFeature.RADAR to "radarAllowed",
        GlobalFeature.WAYPOINTS to "waypointsAllowed",
        GlobalFeature.DEATH_WAYPOINTS to "deathWaypointAllowed",
        GlobalFeature.RADAR_PLAYERS to "radarPlayersAllowed",
        GlobalFeature.RADAR_MOBS to "radarMobsAllowed",
    )

    /**
     * Returns [options] with every entry [global] sets applied, reporting each option it replaces through [hint].
     */
    fun apply(options: Map<String, Any?>, global: GlobalSettings, hint: (String) -> Unit): Map<String, Any?> {
        val overlay = OptionOverlay("voxelmap.yml", null, options, hint)

        for ((feature, allowed) in global.overrides) {
            val id = OPTIONS[feature] ?: continue
            overlay.set(feature, id, allowed)
        }

        return overlay.result()
    }
}
