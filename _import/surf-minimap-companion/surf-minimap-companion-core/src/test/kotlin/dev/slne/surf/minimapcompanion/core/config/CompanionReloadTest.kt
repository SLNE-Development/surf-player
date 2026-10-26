package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.minimapcompanion.core.xaero.XaeroServerProfile
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CompanionReloadTest {

    @Test
    fun `reload parses the reloaded config and reports its warnings`() {
        val config = MinimapCompanionConfig(
            enforcedOptions = EnforcedOptionsConfig(
                minimap = mapOf(
                    "display_radar" to BasicConfigurationNode.root().raw(false),
                    "minimap_ui_scale" to BasicConfigurationNode.root().raw(99),
                )
            )
        )

        val result = CompanionReload.reload { config }

        assertSame(config, result.config)
        assertSame(XaeroServerProfile.of(config), result.profile)
        assertEquals(mapOf<String, Any>("display_radar" to false), result.profile.minimap.values)
        assertEquals(1, result.profile.warnings.size)
    }
}
