package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.minimapcompanion.core.global.GlobalFeature
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapProfile
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapProfile
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
    fun `voxelmap options load from voxelmap yml`(@TempDir folder: Path) {
        folder.resolve("voxelmap.yml").writeText(
            """
            options:
              cavesAllowed: false
              teleportCommand: "tp %p %x %y %z"
            """.trimIndent()
        )

        val config = SpongeConfigManager.yaml(VoxelMapConfig::class.java, folder, "voxelmap.yml").config

        assertEquals(
            mapOf<String, Any>("cavesAllowed" to false, "teleportCommand" to "tp %p %x %y %z"),
            VoxelMapProfile.of(config).values,
        )
    }

    @Test
    fun `default voxelmap yml writes an empty option map`(@TempDir folder: Path) {
        SpongeConfigManager.yaml(VoxelMapConfig::class.java, folder, "voxelmap.yml")
        val written = folder.resolve("voxelmap.yml").readText()

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
    fun `default config yml writes per-mod for every global setting`(@TempDir folder: Path) {
        SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml")
        val written = folder.resolve("config.yml").readText()

        assertTrue("global-settings:" in written, written)
        for (feature in GlobalFeature.entries) {
            assertTrue("${feature.key}: per-mod" in written, written)
        }
    }

    @Test
    fun `config yml from an earlier version gets the global settings as per-mod`(@TempDir folder: Path) {
        folder.resolve("config.yml").writeText("disable-maps: false\n")

        val config = SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml").config

        assertEquals(emptyMap(), GlobalSettings.of(config.globalSettings).overrides)
        assertTrue("cave-mode: per-mod" in folder.resolve("config.yml").readText())
    }

    @Test
    fun `global settings load from config yml`(@TempDir folder: Path) {
        folder.resolve("config.yml").writeText(
            """
            global-settings:
              cave-mode: false
              minimap: true
            """.trimIndent()
        )

        val config = SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml").config

        assertEquals(
            mapOf(GlobalFeature.CAVE_MODE to false, GlobalFeature.MINIMAP to true),
            GlobalSettings.of(config.globalSettings).overrides,
        )
    }

    @Test
    fun `a typo in the global settings stays in config yml`(@TempDir folder: Path) {
        folder.resolve("config.yml").writeText(
            """
            global-settings:
              cave-mode: flase
            """.trimIndent()
        )

        SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml")

        assertTrue("cave-mode: flase" in folder.resolve("config.yml").readText())
    }

    @Test
    fun `default xaero yml writes empty option maps`(@TempDir folder: Path) {
        SpongeConfigManager.yaml(XaeroConfig::class.java, folder, "xaero.yml")
        val written = folder.resolve("xaero.yml").readText()

        assertTrue("minimap: {}" in written, written)
        assertTrue("world-map: {}" in written, written)
    }
}
