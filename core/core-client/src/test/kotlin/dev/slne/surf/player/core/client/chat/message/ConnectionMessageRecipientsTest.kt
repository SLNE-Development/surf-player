package dev.slne.surf.player.core.client.chat.message

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectionMessageRecipientsTest {

    private fun receives(playerAlwaysShow: Boolean = false, viewerSettingEnabled: Boolean = true) =
        ConnectionMessageRecipients.receives(playerAlwaysShow) { viewerSettingEnabled }

    @Test
    fun `viewers follow their setting`() {
        assertTrue(receives())
        assertFalse(receives(viewerSettingEnabled = false))
    }

    @Test
    fun `the message of an always show player reaches viewers despite their setting`() {
        assertTrue(receives(playerAlwaysShow = true, viewerSettingEnabled = false))
    }
}
