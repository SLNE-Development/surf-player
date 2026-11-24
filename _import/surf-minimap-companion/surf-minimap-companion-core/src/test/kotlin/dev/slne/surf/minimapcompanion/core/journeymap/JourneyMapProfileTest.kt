package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.config.JourneyMapConfig
import dev.slne.surf.minimapcompanion.core.global.GlobalFeature
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JourneyMapProfileTest {
    private val warnings = mutableListOf<String>()

    private fun parse(vararg raw: Pair<String, Any?>) = JourneyMapProfile.parse(mapOf(*raw)) { warnings += it }

    @Test
    fun `boolean options take yaml booleans`() {
        assertEquals(mapOf("minimapEnabled" to "false"), parse("minimapEnabled" to false).values)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `server options take ALL or NONE in any case`() {
        assertEquals(mapOf("caveMapping" to "NONE"), parse("caveMapping" to "none").values)
        assertEquals(mapOf("radarEnabled" to "ALL"), parse("radarEnabled" to "ALL").values)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `OPS is rejected since it needs per-player settings`() {
        assertEquals(emptyMap(), parse("caveMapping" to "OPS").values)
        assertTrue("caveMapping" in warnings.single(), warnings.single())
    }

    @Test
    fun `teleport takes every value journeymap knows`() {
        assertEquals(mapOf("teleport" to "WAYPOINT_ONLY"), parse("teleport" to "waypoint_only").values)
        assertEquals(emptyList(), warnings)

        assertEquals(emptyMap(), parse("teleport" to "sometimes").values)
        assertTrue("teleport" in warnings.single(), warnings.single())
    }

    @Test
    fun `integers outside journeymap's range are skipped`() {
        assertEquals(emptyMap(), parse("caveRenderRange" to 33).values)
        assertTrue("caveRenderRange" in warnings.single(), warnings.single())

        assertEquals(mapOf("caveRenderRange" to "32"), parse("caveRenderRange" to 32).values)
        assertEquals(mapOf("radarLateralDistance" to "64"), parse("radarLateralDistance" to 64L).values)
        assertEquals(1, warnings.size)
    }

    @Test
    fun `value of the wrong type is skipped`() {
        assertEquals(emptyMap(), parse("minimapEnabled" to "yes").values)
        assertTrue("minimapEnabled" in warnings.single(), warnings.single())
    }

    @Test
    fun `options the companion sets are skipped`() {
        assertEquals(emptyMap(), parse("journeymapEnabled" to false, "worldPlayerRadar" to "ALL").values)
        assertEquals(2, warnings.size)
    }

    @Test
    fun `unknown options are sent as strings with a warning`() {
        assertEquals(mapOf("fooBar" to "3"), parse("fooBar" to 3).values)
        assertTrue("fooBar" in warnings.single(), warnings.single())
    }

    @Test
    fun `null enforces nothing`() {
        assertEquals(emptyMap(), parse("minimapEnabled" to null).values)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `global settings override the options before they are checked`() {
        val config = JourneyMapConfig(options = mapOf("teleport" to BasicConfigurationNode.root().raw("WAYPOINT_ONLY")))
        val profile = JourneyMapProfile.of(config, GlobalSettings(mapOf(GlobalFeature.TELEPORT to true)))
        assertEquals(mapOf("teleport" to "ALL"), profile.values)
    }

    @Test
    fun `each config and global settings pair is parsed once`() {
        val config = JourneyMapConfig()
        val global = GlobalSettings(mapOf(GlobalFeature.RADAR to false))
        assertSame(JourneyMapProfile.of(config, global), JourneyMapProfile.of(config, global))
    }
}
