package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.minimapcompanion.core.xaero.XaeroServerProfile
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import java.io.IOException
import java.io.UncheckedIOException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CompanionConfigsTest {

    /**
     * A config file in memory that, like the real ones, replaces its config as soon as it loads.
     */
    private class FakeFile<C>(val fileName: String, var current: C, private val load: () -> C) {
        var loads = 0

        fun reloadable() = ReloadableConfig(
            fileName,
            load = {
                loads++
                load().also { current = it }
            },
            current = { current },
            restore = { current = it },
        )
    }

    private fun journeyMapFile() = FakeFile("journeymap.yml", JourneyMapConfig()) { JourneyMapConfig() }

    private fun voxelMapFile() = FakeFile("voxelmap.yml", VoxelMapConfig()) { VoxelMapConfig() }

    private fun broken(): Nothing = throw UncheckedIOException(IOException("mapping values are not allowed here"))

    @Test
    fun `reload parses the reloaded xaero config and reports its warnings`() {
        val xaero = XaeroConfig(
            minimap = mapOf(
                "display_radar" to BasicConfigurationNode.root().raw(false),
                "minimap_ui_scale" to BasicConfigurationNode.root().raw(99),
            )
        )
        val main = MinimapCompanionConfig(disableMaps = true)
        val journeyMap = JourneyMapConfig(options = mapOf("caveMapping" to BasicConfigurationNode.root().raw("NONE")))
        val voxelMap = VoxelMapConfig(options = mapOf("cavesAllowed" to BasicConfigurationNode.root().raw(false)))

        val result = CompanionConfigs.reload(
            FakeFile("config.yml", MinimapCompanionConfig()) { main }.reloadable(),
            FakeFile("xaero.yml", XaeroConfig()) { xaero }.reloadable(),
            FakeFile("journeymap.yml", JourneyMapConfig()) { journeyMap }.reloadable(),
            FakeFile("voxelmap.yml", VoxelMapConfig()) { voxelMap }.reloadable(),
        )

        assertIs<CompanionConfigs.ReloadResult.Reloaded>(result)
        assertSame(main, result.config)
        assertSame(xaero, result.xaero)
        assertSame(XaeroServerProfile.of(xaero, result.globalSettings), result.profile)
        assertEquals(mapOf<String, Any>("display_radar" to false), result.profile.minimap.values)
        assertEquals(1, result.profile.warnings.size)
        assertSame(journeyMap, result.journeyMap)
        assertEquals(mapOf("caveMapping" to "NONE"), result.journeyMapProfile.values)
        assertSame(voxelMap, result.voxelMap)
        assertEquals(mapOf<String, Any>("cavesAllowed" to false), result.voxelMapProfile.values)
    }

    @Test
    fun `reload applies the global settings of the reloaded config yml to every mod`() {
        val main = MinimapCompanionConfig(globalSettings = GlobalSettingsConfig(caveMode = BasicConfigurationNode.root().raw(false)))

        val result = CompanionConfigs.reload(
            FakeFile("config.yml", MinimapCompanionConfig()) { main }.reloadable(),
            FakeFile("xaero.yml", XaeroConfig()) { XaeroConfig() }.reloadable(),
            journeyMapFile().reloadable(),
            voxelMapFile().reloadable(),
        )

        assertIs<CompanionConfigs.ReloadResult.Reloaded>(result)
        assertEquals(mapOf<String, Any>("minimap_cave_mode_allowed" to false), result.profile.minimap.values)
        assertEquals(mapOf<String, Any>("cave_mode_allowed" to false), result.profile.worldMap.values)
        assertEquals(mapOf("caveMapping" to "NONE"), result.journeyMapProfile.values)
        assertEquals(mapOf<String, Any>("cavesAllowed" to false), result.voxelMapProfile.values)
    }

    @Test
    fun `reload reports unknown global values`() {
        val main = MinimapCompanionConfig(globalSettings = GlobalSettingsConfig(radar = BasicConfigurationNode.root().raw("flase")))

        val result = CompanionConfigs.reload(
            FakeFile("config.yml", MinimapCompanionConfig()) { main }.reloadable(),
            FakeFile("xaero.yml", XaeroConfig()) { XaeroConfig() }.reloadable(),
            journeyMapFile().reloadable(),
            voxelMapFile().reloadable(),
        )

        assertIs<CompanionConfigs.ReloadResult.Reloaded>(result)
        assertEquals(listOf("Unknown value \"flase\" for global-settings.radar, using per-mod"), result.globalSettings.warnings)
    }

    @Test
    fun `broken xaero yml keeps the previous config yml active`() {
        val previous = MinimapCompanionConfig()
        val main = FakeFile("config.yml", previous) { MinimapCompanionConfig(disableMaps = true) }
        val xaero = FakeFile("xaero.yml", XaeroConfig()) { broken() }

        val result = CompanionConfigs.reload(main.reloadable(), xaero.reloadable(), journeyMapFile().reloadable(), voxelMapFile().reloadable())

        assertIs<CompanionConfigs.ReloadResult.Failed>(result)
        assertEquals("xaero.yml", result.fileName)
        assertTrue("mapping values are not allowed here" in result.reason, result.reason)
        assertSame(previous, main.current)
    }

    @Test
    fun `broken config yml leaves xaero yml unread`() {
        val main = FakeFile("config.yml", MinimapCompanionConfig()) { broken() }
        val xaero = FakeFile("xaero.yml", XaeroConfig()) { XaeroConfig() }

        val result = CompanionConfigs.reload(main.reloadable(), xaero.reloadable(), journeyMapFile().reloadable(), voxelMapFile().reloadable())

        assertIs<CompanionConfigs.ReloadResult.Failed>(result)
        assertEquals("config.yml", result.fileName)
        assertEquals(0, xaero.loads)
    }

    @Test
    fun `broken journeymap yml keeps the other files' previous configs`() {
        val previousMain = MinimapCompanionConfig()
        val previousXaero = XaeroConfig()
        val main = FakeFile("config.yml", previousMain) { MinimapCompanionConfig(disableMaps = true) }
        val xaero = FakeFile("xaero.yml", previousXaero) { XaeroConfig() }
        val journeyMap = FakeFile("journeymap.yml", JourneyMapConfig()) { broken() }

        val result = CompanionConfigs.reload(main.reloadable(), xaero.reloadable(), journeyMap.reloadable(), voxelMapFile().reloadable())

        assertIs<CompanionConfigs.ReloadResult.Failed>(result)
        assertEquals("journeymap.yml", result.fileName)
        assertSame(previousMain, main.current)
        assertSame(previousXaero, xaero.current)
    }

    @Test
    fun `broken voxelmap yml keeps the other files' previous configs`() {
        val previousMain = MinimapCompanionConfig()
        val previousXaero = XaeroConfig()
        val previousJourneyMap = JourneyMapConfig()
        val main = FakeFile("config.yml", previousMain) { MinimapCompanionConfig(disableMaps = true) }
        val xaero = FakeFile("xaero.yml", previousXaero) { XaeroConfig() }
        val journeyMap = FakeFile("journeymap.yml", previousJourneyMap) { JourneyMapConfig() }
        val voxelMap = FakeFile("voxelmap.yml", VoxelMapConfig()) { broken() }

        val result = CompanionConfigs.reload(main.reloadable(), xaero.reloadable(), journeyMap.reloadable(), voxelMap.reloadable())

        assertIs<CompanionConfigs.ReloadResult.Failed>(result)
        assertEquals("voxelmap.yml", result.fileName)
        assertSame(previousMain, main.current)
        assertSame(previousXaero, xaero.current)
        assertSame(previousJourneyMap, journeyMap.current)
    }
}
