package dev.slne.surf.player.core.common.queue.queue.tick

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SafeQueueTickTest {

    @Test
    fun `a tick job cancelled on shutdown is a stop, not a failure`() {
        var end: TickJobEnd? = null

        runBlocking {
            val job: Job = launch { while (true) delay(10) }
            job.invokeOnCompletion { end = SafeQueueTick.tickJobEnd(it) }
            delay(20)
            job.cancel()
            job.join()
        }

        assertEquals(TickJobEnd.STOPPED, end)
    }

    @Test
    fun `completion and real failures are told apart`() {
        assertEquals(TickJobEnd.COMPLETED, SafeQueueTick.tickJobEnd(null))
        assertEquals(TickJobEnd.STOPPED, SafeQueueTick.tickJobEnd(CancellationException("shutdown")))
        assertEquals(TickJobEnd.FAILED, SafeQueueTick.tickJobEnd(IllegalStateException("redis down")))
    }
}
