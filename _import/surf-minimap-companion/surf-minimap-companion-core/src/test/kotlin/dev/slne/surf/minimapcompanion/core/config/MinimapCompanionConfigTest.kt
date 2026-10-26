package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapProfile
import dev.slne.surf.minimapcompanion.core.xaero.XaeroServerProfile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MinimapCompanionConfigTest {

    @Test
    fun `disable-maps and tracker load from config yml`(@TempDir folder: Path) {
        folder.resolve("config.yml").writeText(
            """
            disable-maps: true
            tracker:
              friends: true
            """.trimIndent()
        )

        val config = SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml").config

        assertEquals(true, config.disableMaps)
        assertEquals(TrackerConfig(friends = true), config.tracker)
    }

    @Test
    fun `enforced options load from xaero yml`(@TempDir folder: Path) {
        folder.resolve("xaero.yml").writeText(
            """
            minimap:
              display_radar: false
              radar_name_scale: 2
            world-map:
              cave_mode_allowed_dimensions:
                - minecraft:overworld
            """.trimIndent()
        )

        val config = SpongeConfigManager.yaml(XaeroConfig::class.java, folder, "xaero.yml").config
        val profile = XaeroServerProfile.of(config)

        assertEquals(mapOf<String, Any>("display_radar" to false, "radar_name_scale" to 2.0), profile.minimap.values)
        assertEquals(setOf("minecraft:overworld"), profile.worldMap.identifiers("cave_mode_allowed_dimensions"))
    }

    @Test
    fun `journeymap options load from journeymap yml`(@TempDir folder: Path) {
        folder.resolve("journeymap.yml").writeText(
            """
            options:
              caveMapping: NONE
              radarLateralDistance: 64
            """.trimIndent()
        )

        val config = SpongeConfigManager.yaml(JourneyMapConfig::class.java, folder, "journeymap.yml").config

        assertEquals(mapOf("caveMapping" to "NONE", "radarLateralDistance" to "64"), JourneyMapProfile.of(config).values)
    }

    @Test
    fun `default journeymap yml writes an empty option map`(@TempDir folder: Path) {
        SpongeConfigManager.yaml(JourneyMapConfig::class.java, folder, "journeymap.yml")
        val written = folder.resolve("journeymap.yml").readText()

        assertTrue("options: {}" in written, written)
    }

    @Test
    fun `default config yml writes maps enabled and the tracker disabled`(@TempDir folder: Path) {
        SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml")
        val written = folder.resolve("config.yml").readText()

        assertTrue("disable-maps: false" in written, written)
        assertTrue("everyone-tracks-everyone: false" in written, written)
        assertTrue("clan-members: false" in written, written)
    }

    @Test
    fun `default xaero yml writes empty option maps`(@TempDir folder: Path) {
        SpongeConfigManager.yaml(XaeroConfig::class.java, folder, "xaero.yml")
        val written = folder.resolve("xaero.yml").readText()

        assertTrue("minimap: {}" in written, written)
        assertTrue("world-map: {}" in written, written)
    }
}
