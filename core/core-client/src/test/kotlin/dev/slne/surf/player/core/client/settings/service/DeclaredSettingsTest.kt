package dev.slne.surf.player.core.client.settings.service

import dev.slne.surf.player.api.settings.setting.Setting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeclaredSettingsTest {

    @Test
    fun `stored settings that no longer exist in the code are left out`() {
        val chatPings = Setting("chat:pings", "true")
        val directMessages = Setting("chat:direct-messages", "true")
        val removed = Setting("scoreboard:show-scoreboard", "true")

        assertEquals(
            listOf(chatPings, directMessages),
            declaredOnly(
                listOf(chatPings, removed, directMessages),
                setOf("chat:pings", "chat:direct-messages", "friends:sounds"),
            )
        )
    }

    @Test
    fun `nothing is shown before a setting is declared`() {
        assertEquals(emptyList<Setting>(), declaredOnly(listOf(Setting("chat:pings", "true")), emptySet()))
    }
}
