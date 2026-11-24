package dev.slne.surf.player.core.client.queue

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.api.queue.SurfQueueAvailableSlotsProvider
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.queue.config.SurfQueueConfig
import dev.slne.surf.player.core.client.queue.metrics.QueueMetricsLogger
import dev.slne.surf.player.core.client.queue.queue.ClientQueueImpl
import dev.slne.surf.player.core.client.queue.queue.DefaultSurfQueueAvailableSlotsProvider
import dev.slne.surf.player.core.client.queue.queue.OwnedClientQueueImpl
import dev.slne.surf.player.core.common.queue.QueueInstance
import dev.slne.surf.player.core.common.queue.queue.AbstractQueue
import dev.slne.surf.player.core.common.queue.queue.tick.QueueScheduler
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.nio.file.Path

/**
 * [QueueInstance] variant for game servers that own and process queues.
 *
 * The instance for the server's own queue ticks transfers and cleanup, while queues of other
 * servers are created as plain, non-ticking queues used for administrative access.
 *
 * Platform-specific subclasses provide the [dataPath].
 */
abstract class ClientQueueInstance : QueueInstance() {

    /** Directory the queue feature stores its configuration in. */
    abstract val dataPath: Path

    override val redisApi get() = SurfPlayerClient.redisApi

    override val queueScheduler = QueueScheduler(workerCount = 1) // Only one tickable queue max, so 1 worker is enough

    /**
     * Loads the configuration. Runs before Redis is connected. Subclasses **must** call
     * `super.load()`.
     */
    @MustBeInvokedByOverriders
    open suspend fun load() {
        SurfQueueConfig.init()
        SurfQueueAvailableSlotsProvider.set(DefaultSurfQueueAvailableSlotsProvider)
    }

    @MustBeInvokedByOverriders
    override suspend fun disable() {
        super.disable()
        QueueMetricsLogger.stop()
    }

    override fun createQueue(serverName: String): AbstractQueue {
        return if (serverName == SurfServer.current().name) {
            OwnedClientQueueImpl(serverName, queueScheduler)
        } else {
            ClientQueueImpl(serverName)
        }
    }

    companion object {
        fun get() = QueueInstance.get() as ClientQueueInstance
    }
}
