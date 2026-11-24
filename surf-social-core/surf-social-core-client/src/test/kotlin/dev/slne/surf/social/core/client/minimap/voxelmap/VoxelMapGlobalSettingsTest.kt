package dev.slne.surf.social.core.client.minimap.voxelmap

import dev.slne.surf.social.core.client.minimap.global.GlobalFeature
import dev.slne.surf.social.core.client.minimap.global.GlobalSettings
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VoxelMapGlobalSettingsTest {
    private val allFalse = GlobalSettings(GlobalFeature.entries.associateWith { false })
    private val allTrue = GlobalSettings(GlobalFeature.entries.associateWith { true })

    private fun apply(options: Map<String, Any?>, global: GlobalSettings): Pair<Map<String, Any?>, List<String>> {
        val hints = mutableListOf<String>()
        return VoxelMapGlobalSettings.apply(options, global) { hints += it } to hints
    }

    @Test
    fun `false forbids every feature voxelmap has`() {
        assertEquals(
            mapOf<String, Any?>(
                "minimapAllowed" to false,
                "cavesAllowed" to false,
                "radarAllowed" to false,
                "waypointsAllowed" to false,
                "deathWaypointAllowed" to false,
                "radarPlayersAllowed" to false,
                "radarMobsAllowed" to false,
            ),
            apply(emptyMap(), allFalse).first,
        )
    }

    @Test
    fun `true overrides voxelmap yml and keeps what no entry covers`() {
        val (options, hints) = apply(
            mapOf("cavesAllowed" to false, "worldmapAllowed" to false, "teleportCommand" to "tp %p %x %y %z"),
            allTrue,
        )
        assertEquals(
            mapOf<String, Any?>(
                "cavesAllowed" to true,
                "worldmapAllowed" to false,
                "teleportCommand" to "tp %p %x %y %z",
                "minimapAllowed" to true,
                "radarAllowed" to true,
                "waypointsAllowed" to true,
                "deathWaypointAllowed" to true,
                "radarPlayersAllowed" to true,
                "radarMobsAllowed" to true,
            ),
            options,
        )
        assertEquals(listOf("global-settings.cave-mode overrides cavesAllowed in voxelmap.yml"), hints)
    }

    @Test
    fun `per-mod leaves voxelmap yml alone`() {
        val options = mapOf<String, Any?>("cavesAllowed" to false)
        assertEquals(options to emptyList<String>(), apply(options, GlobalSettings.NONE))
    }
}
