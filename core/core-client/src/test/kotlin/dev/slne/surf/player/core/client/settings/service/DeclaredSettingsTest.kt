package dev.slne.surf.player.core.client.settings.service

import dev.slne.surf.player.api.settings.setting.Setting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeclaredSettingsTest {

    @Test
    fun `settings from other servers are kept and listed separately`() {
        val chatPings = Setting("chat:pings", "true")
        val directMessages = Setting("chat:direct-messages", "true")
        val otherServer = Setting("lobby:jump-pads", "true")

        assertEquals(
            DeclaredSettings(
                declared = listOf(directMessages, chatPings),
                others = listOf(otherServer),
            ),
            partitionByDeclaration(
                listOf(chatPings, otherServer, directMessages),
                setOf("chat:pings", "chat:direct-messages", "friends:sounds"),
            )
        )
    }

    @Test
    fun `without declarations every setting is an other one`() {
        val chatPings = Setting("chat:pings", "true")
        assertEquals(
            DeclaredSettings(emptyList(), listOf(chatPings)),
            partitionByDeclaration(listOf(chatPings), emptySet()),
        )
    }
}
