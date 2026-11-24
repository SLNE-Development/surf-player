package dev.slne.surf.player.core.client.command

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.rabbitmq.api.exception.SurfRabbitConnectionException
import dev.slne.surf.rabbitmq.api.exception.SurfRabbitRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * How long a command waits for the microservice before telling the sender that the service is not
 * reachable. The RabbitMQ request timeout of the connection (60 s by default) is meant for
 * background work; nobody waits that long for a command.
 */
val INTERACTIVE_RPC_TIMEOUT: Duration = 10.seconds

/**
 * The microservice (or RabbitMQ) did not answer within the time an interactive call may wait.
 */
class ServiceUnavailableException(cause: Throwable?) :
    RuntimeException("The surf-player microservice is not reachable", cause)

/**
 * Whether this failure means the microservice or RabbitMQ could not be reached: a request that
 * timed out, a connection that is down, or an interactive call that waited too long.
 */
fun Throwable.isServiceUnavailable(): Boolean = generateSequence(this) { it.cause }
    .take(16)
    .any {
        it is ServiceUnavailableException ||
            it is SurfRabbitRequestTimeoutException ||
            it is SurfRabbitConnectionException
    }

private val log = logger()

/**
 * Runs [block], which calls the microservice, for a command: it may take at most [timeout] (no
 * limit when `null`). If the microservice cannot be reached, [onUnavailable] is called instead of
 * letting the failure through, so every command reports it with
 * [CommandMessages.SERVICE_UNAVAILABLE]; any other failure is rethrown unchanged.
 */
suspend fun <T> runServiceCall(
    timeout: Duration? = INTERACTIVE_RPC_TIMEOUT,
    onUnavailable: (Throwable) -> Nothing,
    block: suspend CoroutineScope.() -> T,
): T {
    val failure = try {
        return if (timeout == null) coroutineScope(block) else withTimeout(timeout, block)
    } catch (e: TimeoutCancellationException) {
        ServiceUnavailableException(e)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (!e.isServiceUnavailable()) throw e
        e
    }

    log.atWarning().log("surf-player microservice not reachable: %s", failure.toString())
    onUnavailable(failure)
}
