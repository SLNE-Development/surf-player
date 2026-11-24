package dev.slne.surf.player.core.client.command

import dev.slne.surf.rabbitmq.api.exception.SurfRabbitConnectionException
import dev.slne.surf.rabbitmq.api.exception.SurfRabbitRequestTimeoutException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ServiceCallsTest {

    private class Unavailable(cause: Throwable) : RuntimeException(cause)

    private fun <T> call(timeout: kotlin.time.Duration? = 1.seconds, block: suspend () -> T): T = runBlocking {
        runServiceCall(timeout, { throw Unavailable(it) }) { block() }
    }

    @Test
    fun `a call that answers returns its result`() {
        assertEquals(42, call { 42 })
    }

    @Test
    fun `a call that waits too long is reported as unavailable`() {
        val failure = assertThrows<Unavailable> { call(50.milliseconds) { awaitCancellation() } }

        assertTrue(failure.cause is ServiceUnavailableException)
    }

    @Test
    fun `a request timeout of the connection is reported as unavailable`() {
        assertThrows<Unavailable> { call { throw SurfRabbitRequestTimeoutException(null, 60.seconds) } }
    }

    @Test
    fun `a broken connection is reported as unavailable, also when wrapped`() {
        assertThrows<Unavailable> { call { throw SurfRabbitConnectionException("down") } }
        assertThrows<Unavailable> {
            call { throw IllegalStateException("rpc failed", SurfRabbitConnectionException("down")) }
        }
    }

    @Test
    fun `other failures pass through unchanged`() {
        assertThrows<IllegalArgumentException> { call { throw IllegalArgumentException("bug") } }
    }

    @Test
    fun `without a timeout the call may take its time`() {
        assertEquals("done", call(timeout = null) { kotlinx.coroutines.delay(20); "done" })
    }

    @Test
    fun `only connection problems count as unavailable`() {
        assertTrue(SurfRabbitConnectionException("down").isServiceUnavailable())
        assertFalse(IllegalStateException("bug").isServiceUnavailable())
    }

    @Test
    fun `the message is the shared German one`() {
        assertEquals(
            "Der Dienst ist gerade nicht erreichbar. Bitte versuche es später erneut.",
            CommandMessages.SERVICE_UNAVAILABLE
        )
    }
}
