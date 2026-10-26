package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
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
    fun `enforced options and tracker load from yaml`(@TempDir folder: Path) {
        folder.resolve("config.yml").writeText(
            """
            tracker:
              friends: true
            enforced-options:
              minimap:
                display_radar: false
                radar_name_scale: 2
              world-map:
                cave_mode_allowed_dimensions:
                  - minecraft:overworld
            """.trimIndent()
        )

        val config = SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml").config
        val profile = XaeroServerProfile.of(config)

        assertEquals(TrackerConfig(friends = true), config.tracker)
        assertEquals(mapOf<String, Any>("display_radar" to false, "radar_name_scale" to 2.0), profile.minimap.values)
        assertEquals(setOf("minecraft:overworld"), profile.worldMap.identifiers("cave_mode_allowed_dimensions"))
    }

    @Test
    fun `default config writes empty option maps and disabled tracker`(@TempDir folder: Path) {
        SpongeConfigManager.yaml(MinimapCompanionConfig::class.java, folder, "config.yml")
        val written = folder.resolve("config.yml").readText()

        assertTrue("everyone-tracks-everyone: false" in written, written)
        assertTrue("clan-members: false" in written, written)
        assertTrue("world-map: {}" in written, written)
    }
}
