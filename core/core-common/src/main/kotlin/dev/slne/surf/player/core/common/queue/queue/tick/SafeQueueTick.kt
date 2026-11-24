package dev.slne.surf.player.core.common.queue.queue.tick

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.core.common.queue.queue.AbstractQueue
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

/** How a queue's tick job ended, see [SafeQueueTick.tickJobEnd]. */
enum class TickJobEnd { COMPLETED, STOPPED, FAILED }

object SafeQueueTick {
    val log = logger()

    /**
     * How a tick job that completed with [cause] ended: a [CancellationException] is the normal
     * stop on shutdown (or when the queue is disabled), not a failure.
     */
    fun tickJobEnd(cause: Throwable?): TickJobEnd = when (cause) {
        null -> TickJobEnd.COMPLETED
        is CancellationException -> TickJobEnd.STOPPED
        else -> TickJobEnd.FAILED
    }

    inline fun tickSafe(queue: AbstractQueue, component: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.atWarning()
                .withCause(e)
                .log("Failed to tick %s for queue %s", component, queue.serverName)
        }
    }

    suspend inline fun tickSafeWithTimeout(
        queue: AbstractQueue,
        component: String,
        timeout: Duration,
        crossinline block: suspend () -> Unit
    ) {
        try {
            withTimeout(timeout) {
                block()
            }
        } catch (_: TimeoutCancellationException) {
            log.atWarning()
                .log("Timed out ticking %s for queue %s", component, queue.serverName)
        } catch (e: CancellationException) {
            if (!currentCoroutineContext().isActive) throw e
            log.atWarning()
                .withCause(e)
                .log("Cancelled ticking %s for queue %s", component, queue.serverName)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to tick %s for queue %s", component, queue.serverName)
        }
    }
}