package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.global.GlobalFeature
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class XaeroGlobalSettingsTest {
    private val allFalse = GlobalSettings(GlobalFeature.entries.associateWith { false })
    private val allTrue = GlobalSettings(GlobalFeature.entries.associateWith { true })

    private fun apply(
        minimap: Map<String, Any?>,
        worldMap: Map<String, Any?>,
        global: GlobalSettings,
    ): Triple<Map<String, Any?>, Map<String, Any?>, List<String>> {
        val hints = mutableListOf<String>()
        val (m, w) = XaeroGlobalSettings.apply(minimap, worldMap, global) { hints += it }
        return Triple(m, w, hints)
    }

    @Test
    fun `false enforces the off value of every feature xaero has`() {
        val (minimap, worldMap, _) = apply(emptyMap(), emptyMap(), allFalse)
        assertEquals(
            mapOf<String, Any?>(
                "display_minimap" to false,
                "minimap_cave_mode_allowed" to false,
                "display_radar" to false,
                "waypoints_in_world" to false,
                "waypoints_on_minimap" to false,
                "deathpoints" to false,
                "hide_waypoint_coordinates" to true,
            ),
            minimap,
        )
        assertEquals(
            mapOf<String, Any?>(
                "cave_mode_allowed" to false,
                "waypoints" to false,
                "map_teleport_allowed" to false,
                "display_coordinates" to false,
            ),
            worldMap,
        )
    }

    @Test
    fun `true lifts every option the entries cover and keeps the rest`() {
        val (minimap, worldMap, _) = apply(
            mapOf(
                "display_minimap" to false,
                "minimap_cave_mode_allowed_dimensions" to listOf("minecraft:overworld"),
                "minimap_ui_scale" to 3,
            ),
            mapOf("cave_mode_allowed" to false, "cave_mode_allowed_dimensions" to "overworld", "lighting" to false),
            allTrue,
        )
        assertEquals(mapOf<String, Any?>("minimap_ui_scale" to 3), minimap)
        assertEquals(mapOf<String, Any?>("lighting" to false), worldMap)
    }

    @Test
    fun `lifting an option the file enforces is reported even when it allows`() {
        val (_, _, hints) = apply(mapOf("display_minimap" to true), emptyMap(), GlobalSettings(mapOf(GlobalFeature.MINIMAP to true)))
        assertEquals(listOf("global-settings.minimap overrides minimap.display_minimap in xaero.yml"), hints)
    }

    @Test
    fun `forbidding what the file already forbids is not reported`() {
        val (_, _, hints) = apply(emptyMap(), mapOf("cave_mode_allowed" to false), GlobalSettings(mapOf(GlobalFeature.CAVE_MODE to false)))
        assertEquals(emptyList(), hints)
    }

    @Test
    fun `per-mod leaves xaero yml alone`() {
        val minimap = mapOf<String, Any?>("display_radar" to false)
        val worldMap = mapOf<String, Any?>("cave_mode_allowed" to true)
        assertEquals(Triple(minimap, worldMap, emptyList<String>()), apply(minimap, worldMap, GlobalSettings.NONE))
    }
}
