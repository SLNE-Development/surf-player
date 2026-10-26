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

        val result = CompanionConfigs.reload(
            FakeFile("config.yml", MinimapCompanionConfig()) { main }.reloadable(),
            FakeFile("xaero.yml", XaeroConfig()) { xaero }.reloadable(),
        )

        assertIs<CompanionConfigs.ReloadResult.Reloaded>(result)
        assertSame(main, result.config)
        assertSame(xaero, result.xaero)
        assertSame(XaeroServerProfile.of(xaero), result.profile)
        assertEquals(mapOf<String, Any>("display_radar" to false), result.profile.minimap.values)
        assertEquals(1, result.profile.warnings.size)
    }

    @Test
    fun `broken xaero yml keeps the previous config yml active`() {
        val previous = MinimapCompanionConfig()
        val main = FakeFile("config.yml", previous) { MinimapCompanionConfig(disableMaps = true) }
        val xaero = FakeFile("xaero.yml", XaeroConfig()) { broken() }

        val result = CompanionConfigs.reload(main.reloadable(), xaero.reloadable())

        assertIs<CompanionConfigs.ReloadResult.Failed>(result)
        assertEquals("xaero.yml", result.fileName)
        assertTrue("mapping values are not allowed here" in result.reason, result.reason)
        assertSame(previous, main.current)
    }

    @Test
    fun `broken config yml leaves xaero yml unread`() {
        val main = FakeFile("config.yml", MinimapCompanionConfig()) { broken() }
        val xaero = FakeFile("xaero.yml", XaeroConfig()) { XaeroConfig() }

        val result = CompanionConfigs.reload(main.reloadable(), xaero.reloadable())

        assertIs<CompanionConfigs.ReloadResult.Failed>(result)
        assertEquals("config.yml", result.fileName)
        assertEquals(0, xaero.loads)
    }
}
