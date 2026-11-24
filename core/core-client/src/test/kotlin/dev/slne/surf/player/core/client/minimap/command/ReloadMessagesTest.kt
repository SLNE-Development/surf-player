package dev.slne.surf.player.core.client.minimap.command

import dev.slne.surf.api.core.messages.adventure.plain
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.config.JourneyMapConfig
import dev.slne.surf.player.core.client.minimap.config.MinimapCompanionConfig
import dev.slne.surf.player.core.client.minimap.config.ReloadResult
import dev.slne.surf.player.core.client.minimap.config.ReloadableConfig
import dev.slne.surf.player.core.client.minimap.config.VoxelMapConfig
import dev.slne.surf.player.core.client.minimap.config.XaeroConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ReloadMessagesTest {

    private fun <C> file(name: String, config: C) = ReloadableConfig(name, { config }, { config }, {})

    @Test
    fun `the shared reload message names the feature`() {
        assertTrue(
            CommandMessages.configReloaded("minimap", 6).plain()
                .endsWith("Die Konfiguration von minimap wurde neu geladen (6ms).")
        )
    }

    @Test
    fun `a successful minimap reload uses the shared reload message`() {
        val result = CompanionConfigs.reload(
            file("config.yml", MinimapCompanionConfig()),
            file("xaero.yml", XaeroConfig()),
            file("journeymap.yml", JourneyMapConfig()),
            file("voxelmap.yml", VoxelMapConfig()),
        )
        assertIs<ReloadResult.Reloaded>(result)

        val first = reloadResultMessages(result).first()

        assertEquals(
            CommandMessages.configReloaded("minimap", result.duration.inWholeMilliseconds).plain(),
            first.plain()
        )
    }

    @Test
    fun `a failed minimap reload uses the shared failure message`() {
        val messages = reloadResultMessages(ReloadResult.Failed("xaero.yml", "kaputt", 3.milliseconds))

        assertEquals(1, messages.size)
        assertTrue(
            messages.single().plain().contains(
                "Die Konfiguration von minimap konnte nicht neu geladen werden. Die bisherige Konfiguration bleibt aktiv."
            )
        )
        assertTrue(messages.single().plain().contains("xaero.yml: kaputt"))
    }
}
