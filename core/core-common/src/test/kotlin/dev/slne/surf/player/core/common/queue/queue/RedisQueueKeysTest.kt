package dev.slne.surf.player.core.common.queue.queue

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class RedisQueueKeysTest {
    private val keys = RedisQueueKeys("survival01")

    @Test
    fun `clearing a queue removes every key holding queued players`() {
        assertEquals(
            setOf(keys.entriesKey, keys.metaKey, keys.lastSeenKey, keys.retryCountKey),
            keys.entryKeys.toSet()
        )
    }

    @Test
    fun `clearing a queue keeps its paused state`() {
        assertFalse(keys.pausedKey in keys.entryKeys)
    }

    @Test
    fun `clearing a queue keeps its epoch and locks`() {
        assertFalse(keys.epochMsKey in keys.entryKeys)
        assertFalse(keys.transferSemaphoreKey in keys.entryKeys)
        assertFalse(keys.cleanupSemaphoreKey in keys.entryKeys)
    }
}
