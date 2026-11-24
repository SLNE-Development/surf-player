package dev.slne.surf.player.core.client.chat.processor.pre.validate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.time.Duration.Companion.minutes

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

    private var time = 0L
    private val sender = UUID.randomUUID()
    private val guard = SpamGuard(intervalMillis = { 3_000 }, amount = { 3 }, repeatMemory = 10.minutes, now = { time })

    @Test
    fun `different messages within the limit are allowed`() {
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "a"))
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "b"))
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "c"))
        assertEquals(SpamVerdict.TOO_FAST, guard.check(sender, "d"))
    }

    @Test
    fun `spamming the same message only ever reports the repeat`() {
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "hallo"))

        repeat(10) {
            time += 10
            assertEquals(SpamVerdict.REPEATED, guard.check(sender, "HALLO"), "attempt $it")
        }
    }

    @Test
    fun `blocked repeats do not count towards the rate limit`() {
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "a"))
        repeat(5) { guard.check(sender, "a") }

        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "b"))
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "c"))
        assertEquals(SpamVerdict.TOO_FAST, guard.check(sender, "d"))
    }

    @Test
    fun `a message blocked for the rate limit is no repeat afterwards`() {
        guard.check(sender, "a")
        guard.check(sender, "b")
        guard.check(sender, "c")
        assertEquals(SpamVerdict.TOO_FAST, guard.check(sender, "d"))

        time += 3_000
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "d"))
    }

    @Test
    fun `the rate limit resets after the interval`() {
        guard.check(sender, "a")
        guard.check(sender, "b")
        guard.check(sender, "c")
        assertEquals(SpamVerdict.TOO_FAST, guard.check(sender, "d"))

        time += 3_000
        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "e"))
    }

    @Test
    fun `the repeat is forgotten after the repeat memory`() {
        guard.check(sender, "hallo")
        time += 10.minutes.inWholeMilliseconds

        assertEquals(SpamVerdict.ALLOWED, guard.check(sender, "hallo"))
    }

    @Test
    fun `senders are tracked separately`() {
        val other = UUID.randomUUID()
        guard.check(sender, "hallo")

        assertEquals(SpamVerdict.ALLOWED, guard.check(other, "hallo"))
    }
}
