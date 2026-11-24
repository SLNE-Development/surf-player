package dev.slne.surf.player.core.client.minimap.xaero

import dev.slne.surf.player.core.client.minimap.global.GlobalFeature
import dev.slne.surf.player.core.client.minimap.global.GlobalSettings
import dev.slne.surf.player.core.client.minimap.global.OptionOverlay

/**
 * An option one entry covers: [off] is the value that turns the feature off. [liftOnly] options are only removed,
 * by `true`.
 */
private data class XaeroGlobalOption(val worldMap: Boolean, val id: String, val off: Boolean = false, val liftOnly: Boolean = false)

/**
 * Applies `global-settings` to the options of `xaero.yml`. An enforced Xaero option locks the player's own setting,
 * so `false` enforces the value that turns a feature off, and `true` stops enforcing the feature's options instead of
 * forcing it on.
 */
object XaeroGlobalSettings {

    private val OPTIONS = mapOf(
        GlobalFeature.MINIMAP to listOf(XaeroGlobalOption(false, "display_minimap")),
        GlobalFeature.CAVE_MODE to listOf(
            XaeroGlobalOption(false, "minimap_cave_mode_allowed"),
            XaeroGlobalOption(false, "minimap_cave_mode_allowed_dimensions", liftOnly = true),
            XaeroGlobalOption(true, "cave_mode_allowed"),
            XaeroGlobalOption(true, "cave_mode_allowed_dimensions", liftOnly = true),
        ),
        GlobalFeature.RADAR to listOf(XaeroGlobalOption(false, "display_radar")),
        GlobalFeature.WAYPOINTS to listOf(
            XaeroGlobalOption(false, "waypoints_in_world"),
            XaeroGlobalOption(false, "waypoints_on_minimap"),
            XaeroGlobalOption(true, "waypoints"),
        ),
        GlobalFeature.DEATH_WAYPOINTS to listOf(XaeroGlobalOption(false, "deathpoints")),
        GlobalFeature.TELEPORT to listOf(XaeroGlobalOption(true, "map_teleport_allowed")),
        GlobalFeature.COORDINATES to listOf(
            XaeroGlobalOption(false, "hide_waypoint_coordinates", off = true),
            XaeroGlobalOption(true, "display_coordinates"),
        ),
    )

    /**
     * Returns the [minimap] and [worldMap] options with every entry [global] sets applied, reporting each option it
     * replaces through [hint].
     */
    fun apply(
        minimap: Map<String, Any?>,
        worldMap: Map<String, Any?>,
        global: GlobalSettings,
        hint: (String) -> Unit,
    ): Pair<Map<String, Any?>, Map<String, Any?>> {
        val minimapOverlay = OptionOverlay("xaero.yml", "minimap", minimap, hint)
        val worldMapOverlay = OptionOverlay("xaero.yml", "world-map", worldMap, hint)

        for ((feature, allowed) in global.overrides) {
            for (option in OPTIONS[feature].orEmpty()) {
                val overlay = if (option.worldMap) worldMapOverlay else minimapOverlay
                when {
                    allowed -> overlay.lift(feature, option.id)
                    !option.liftOnly -> overlay.set(feature, option.id, option.off)
                }
            }
        }

        return minimapOverlay.result() to worldMapOverlay.result()
    }
}
