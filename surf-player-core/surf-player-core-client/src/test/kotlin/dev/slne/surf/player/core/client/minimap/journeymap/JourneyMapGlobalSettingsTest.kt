package dev.slne.surf.social.core.client.minimap.journeymap

import dev.slne.surf.social.core.client.minimap.global.GlobalFeature
import dev.slne.surf.social.core.client.minimap.global.GlobalSettings
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JourneyMapGlobalSettingsTest {
    private val allFalse = GlobalSettings(GlobalFeature.entries.associateWith { false })
    private val allTrue = GlobalSettings(GlobalFeature.entries.associateWith { true })

    private fun apply(options: Map<String, Any?>, global: GlobalSettings): Pair<Map<String, Any?>, List<String>> {
        val hints = mutableListOf<String>()
        return JourneyMapGlobalSettings.apply(options, global) { hints += it } to hints
    }

    @Test
    fun `false forbids every feature journeymap has`() {
        assertEquals(
            mapOf<String, Any?>(
                "minimapEnabled" to false,
                "caveMapping" to "NONE",
                "radarEnabled" to "NONE",
                "allowWaypoints" to false,
                "allowDeathPoints" to false,
                "teleport" to "NONE",
                "playerRadarEnabled" to false,
                "mobRadarEnabled" to false,
                "animalRadarEnabled" to false,
                "villagerRadarEnabled" to false,
                "hideCoordinates" to true,
            ),
            apply(emptyMap(), allFalse).first,
        )
    }

    @Test
    fun `true allows every feature journeymap has`() {
        assertEquals(
            mapOf<String, Any?>(
                "minimapEnabled" to true,
                "caveMapping" to "ALL",
                "radarEnabled" to "ALL",
                "allowWaypoints" to true,
                "allowDeathPoints" to true,
                "teleport" to "ALL",
                "playerRadarEnabled" to true,
                "mobRadarEnabled" to true,
                "animalRadarEnabled" to true,
                "villagerRadarEnabled" to true,
                "hideCoordinates" to false,
            ),
            apply(emptyMap(), allTrue).first,
        )
    }

    @Test
    fun `global entries override journeymap yml in both directions`() {
        val (options, hints) = apply(
            mapOf("caveMapping" to "NONE", "allowWaypoints" to true, "radarLateralDistance" to 64),
            GlobalSettings(mapOf(GlobalFeature.CAVE_MODE to true, GlobalFeature.WAYPOINTS to false)),
        )
        assertEquals(mapOf<String, Any?>("caveMapping" to "ALL", "allowWaypoints" to false, "radarLateralDistance" to 64), options)
        assertEquals(
            listOf(
                "global-settings.cave-mode overrides caveMapping in journeymap.yml",
                "global-settings.waypoints overrides allowWaypoints in journeymap.yml",
            ),
            hints,
        )
    }

    @Test
    fun `the same value in another case is not reported`() {
        assertEquals(emptyList(), apply(mapOf("caveMapping" to "none"), GlobalSettings(mapOf(GlobalFeature.CAVE_MODE to false))).second)
    }

    @Test
    fun `per-mod leaves journeymap yml alone`() {
        val options = mapOf<String, Any?>("caveMapping" to "NONE")
        assertEquals(options to emptyList<String>(), apply(options, GlobalSettings.NONE))
    }
}
