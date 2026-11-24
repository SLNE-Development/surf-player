package dev.slne.surf.player.core.client.minimap.global

import dev.slne.surf.player.core.client.minimap.config.GlobalSettingsConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GlobalSettingsTest {

    private fun parse(raw: Map<GlobalFeature, Any?>): Pair<GlobalSettings, List<String>> {
        val warnings = mutableListOf<String>()
        return GlobalSettings.parse(raw) { warnings += it } to warnings
    }

    @Test
    fun `entries use these config keys in this order`() {
        assertEquals(
            listOf("minimap", "cave-mode", "radar", "waypoints", "death-waypoints", "teleport", "radar-players", "radar-mobs", "coordinates"),
            GlobalFeature.entries.map { it.key },
        )
    }

    @Test
    fun `true and false override, per-mod in any case does not`() {
        val (settings, warnings) = parse(
            mapOf(
                GlobalFeature.MINIMAP to true,
                GlobalFeature.CAVE_MODE to false,
                GlobalFeature.RADAR to "per-mod",
                GlobalFeature.WAYPOINTS to "PER-MOD",
            )
        )
        assertEquals(mapOf(GlobalFeature.MINIMAP to true, GlobalFeature.CAVE_MODE to false), settings.overrides)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `quoted true and false count like booleans`() {
        val (settings, warnings) = parse(mapOf(GlobalFeature.RADAR to "false", GlobalFeature.TELEPORT to "TRUE"))
        assertEquals(mapOf(GlobalFeature.RADAR to false, GlobalFeature.TELEPORT to true), settings.overrides)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `unknown value warns and acts as per-mod`() {
        val (settings, warnings) = parse(mapOf(GlobalFeature.CAVE_MODE to "flase"))
        assertEquals(emptyMap(), settings.overrides)
        assertEquals(listOf("Unknown value \"flase\" for global-settings.cave-mode, using per-mod"), warnings)
    }

    @Test
    fun `missing value acts as per-mod`() {
        val (settings, warnings) = parse(mapOf(GlobalFeature.CAVE_MODE to null))
        assertEquals(emptyMap(), settings.overrides)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `every entry has a node in the config`() {
        assertEquals(GlobalFeature.entries.toSet(), GlobalSettingsConfig().entries().keys)
    }

    @Test
    fun `each config is parsed once`() {
        val config = GlobalSettingsConfig()
        assertSame(GlobalSettings.of(config), GlobalSettings.of(config))
    }
}
