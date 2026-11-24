package dev.slne.surf.player.core.client.chat.message

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConnectionMessageHandlingTest {

    private fun mode(
        vanillaMessagePresent: Boolean = true,
        configEnabled: Boolean = true,
        playerVanished: Boolean = false,
    ) = ConnectionMessageHandling.mode(vanillaMessagePresent, configEnabled, playerVanished)

    @Test
    fun `normal join is announced`() {
        assertEquals(ConnectionMessageMode.ANNOUNCE, mode())
    }

    @Test
    fun `message removed by another plugin is left alone`() {
        assertEquals(ConnectionMessageMode.LEAVE_UNTOUCHED, mode(vanillaMessagePresent = false))
        assertEquals(
            ConnectionMessageMode.LEAVE_UNTOUCHED,
            mode(vanillaMessagePresent = false, configEnabled = false),
        )
    }

    @Test
    fun `disabled connection messages are suppressed`() {
        assertEquals(ConnectionMessageMode.SUPPRESS, mode(configEnabled = false))
    }

    @Test
    fun `vanished players are suppressed even though their vanilla message is gone`() {
        assertEquals(
            ConnectionMessageMode.SUPPRESS,
            mode(vanillaMessagePresent = false, playerVanished = true),
        )
        assertEquals(ConnectionMessageMode.SUPPRESS, mode(playerVanished = true))
    }
}
