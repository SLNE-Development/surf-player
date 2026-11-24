package dev.slne.surf.player.core.client.chat.processor.pre.validate

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpamPreChatProcessorTest {

    @Test
    fun `exactly amount messages per interval are allowed`() {
        for (sent in 1..5) {
            assertFalse(exceedsRateLimit(messagesSent = sent, amount = 5), "message $sent of 5")
        }
    }

    @Test
    fun `the message after amount is blocked`() {
        assertTrue(exceedsRateLimit(messagesSent = 6, amount = 5))
    }

    @Test
    fun `a limit of one allows a single message`() {
        assertFalse(exceedsRateLimit(messagesSent = 1, amount = 1))
        assertTrue(exceedsRateLimit(messagesSent = 2, amount = 1))
    }

    @Test
    fun `a repeat ignores case`() {
        assertTrue(isRepeatOf("Hallo", "hallo"))
        assertFalse(isRepeatOf(null, "hallo"))
        assertFalse(isRepeatOf("Hallo", "Hallo!"))
    }
}
