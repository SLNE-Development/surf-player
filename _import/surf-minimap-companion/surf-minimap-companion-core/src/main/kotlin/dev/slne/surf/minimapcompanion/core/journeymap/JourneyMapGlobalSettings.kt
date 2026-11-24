package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.global.GlobalFeature
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import dev.slne.surf.minimapcompanion.core.global.OptionOverlay

/**
 * Applies `global-settings` to the options of `journeymap.yml`. JourneyMap's options are permissions, so each entry
 * sends the value that allows or forbids its feature.
 */
object JourneyMapGlobalSettings {

    /**
     * An option one entry covers, with the values that allow and forbid the feature, as [JourneyMapProfile.parse]
     * takes them.
     */
    private data class Option(val id: String, val allowed: Any, val forbidden: Any)

    private fun switch(id: String) = Option(id, allowed = true, forbidden = false)
    private fun everyone(id: String) = Option(id, allowed = "ALL", forbidden = "NONE")

    private val OPTIONS = mapOf(
        GlobalFeature.MINIMAP to listOf(switch("minimapEnabled")),
        GlobalFeature.CAVE_MODE to listOf(everyone("caveMapping")),
        GlobalFeature.RADAR to listOf(everyone("radarEnabled")),
        GlobalFeature.WAYPOINTS to listOf(switch("allowWaypoints")),
        GlobalFeature.DEATH_WAYPOINTS to listOf(switch("allowDeathPoints")),
        GlobalFeature.TELEPORT to listOf(everyone("teleport")),
        GlobalFeature.RADAR_PLAYERS to listOf(switch("playerRadarEnabled")),
        // VoxelMap's switch covers hostile and neutral mobs, so the same entry hides the same creatures here.
        GlobalFeature.RADAR_MOBS to listOf(switch("mobRadarEnabled"), switch("animalRadarEnabled"), switch("villagerRadarEnabled")),
        GlobalFeature.COORDINATES to listOf(Option("hideCoordinates", allowed = false, forbidden = true)),
    )

    /**
     * Returns [options] with every entry [global] sets applied, reporting each option it replaces through [hint].
     */
    fun apply(options: Map<String, Any?>, global: GlobalSettings, hint: (String) -> Unit): Map<String, Any?> {
        val overlay = OptionOverlay("journeymap.yml", null, options, hint)

        for ((feature, allowed) in global.overrides) {
            for (option in OPTIONS[feature].orEmpty()) {
                overlay.set(feature, option.id, if (allowed) option.allowed else option.forbidden)
            }
        }

        return overlay.result()
    }
}
