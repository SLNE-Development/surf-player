package dev.slne.surf.player.core.client.chat.message

import dev.slne.surf.player.api.chat.message.MessageType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DisabledChatTest {

    @Test
    fun `global messages are blocked while the chat is off`() {
        assertTrue(isBlockedByDisabledChat(MessageType.GLOBAL, localChatEnabled = false) { false })
    }

    @Test
    fun `the bypass writes in a disabled chat`() {
        assertFalse(isBlockedByDisabledChat(MessageType.GLOBAL, localChatEnabled = false) { true })
    }

    @Test
    fun `other message types and an enabled chat are not affected`() {
        assertFalse(isBlockedByDisabledChat(MessageType.DIRECT, localChatEnabled = false) { false })
        assertFalse(isBlockedByDisabledChat(MessageType.GLOBAL, localChatEnabled = true) { error("must not be asked") })
    }
}
