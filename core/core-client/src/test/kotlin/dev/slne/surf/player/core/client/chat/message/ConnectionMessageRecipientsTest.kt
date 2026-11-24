package dev.slne.surf.player.core.client.chat.message

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectionMessageRecipientsTest {

    private fun receives(
        configEnabled: Boolean = true,
        playerVanished: Boolean = false,
        viewerIsPlayer: Boolean = false,
        viewerAlwaysShow: Boolean = false,
        viewerSettingEnabled: Boolean = true,
        viewerCanSeeVanished: Boolean = false,
    ) = ConnectionMessageRecipients.receives(
        configEnabled,
        playerVanished,
        viewerIsPlayer,
        viewerAlwaysShow,
        { viewerSettingEnabled },
        { viewerCanSeeVanished },
    )

    @Test
    fun `normal viewers follow the config and their setting`() {
        assertTrue(receives())
        assertFalse(receives(viewerSettingEnabled = false))
        assertFalse(receives(configEnabled = false))
    }

    @Test
    fun `always show viewers see the message despite their setting`() {
        assertTrue(receives(viewerAlwaysShow = true, viewerSettingEnabled = false))
    }

    @Test
    fun `always show viewers see the message despite the config`() {
        assertTrue(receives(viewerAlwaysShow = true, configEnabled = false))
    }

    @Test
    fun `nobody without the permission sees the message of a vanished player`() {
        assertFalse(receives(playerVanished = true))
        assertFalse(receives(playerVanished = true, viewerCanSeeVanished = true))
    }

    @Test
    fun `always show viewers see a vanished player's message only if they may see vanished players`() {
        assertTrue(receives(playerVanished = true, viewerAlwaysShow = true, viewerCanSeeVanished = true))
        assertFalse(receives(playerVanished = true, viewerAlwaysShow = true, viewerCanSeeVanished = false))
    }

    @Test
    fun `a vanished player does not get their own connection message`() {
        assertFalse(
            receives(playerVanished = true, viewerIsPlayer = true, viewerAlwaysShow = true, viewerCanSeeVanished = true)
        )
    }
}
