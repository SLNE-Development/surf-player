package dev.slne.surf.social.core.client.queue

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.social.api.queue.SurfQueueAvailableSlotsProvider
import dev.slne.surf.social.core.client.queue.config.SurfQueueConfig
import dev.slne.surf.social.core.client.queue.metrics.QueueMetricsLogger
import dev.slne.surf.social.core.client.queue.queue.ClientQueueImpl
import dev.slne.surf.social.core.client.queue.queue.DefaultSurfQueueAvailableSlotsProvider
import dev.slne.surf.social.core.client.queue.queue.OwnedClientQueueImpl
import dev.slne.surf.social.core.common.queue.QueueInstance
import dev.slne.surf.social.core.common.queue.queue.AbstractQueue
import dev.slne.surf.social.core.common.queue.queue.tick.QueueScheduler
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.nio.file.Path

/**
 * [QueueInstance] variant for game servers that own and process queues.
 *
 * The instance for the server's own queue ticks transfers and cleanup, while queues of other
 * servers are created as plain, non-ticking queues used for administrative access.
 *
 * Platform-specific subclasses provide the [componentOwner] and the [dataPath].
 */
abstract class ClientQueueInstance : QueueInstance() {

    /** Directory the plugin stores its configuration in. */
    abstract val dataPath: Path

    override val queueScheduler = QueueScheduler(workerCount = 1) // Only one tickable queue max, so 1 worker is enough

    @MustBeInvokedByOverriders
    override suspend fun load() {
        SurfQueueConfig.init()
        SurfQueueAvailableSlotsProvider.set(DefaultSurfQueueAvailableSlotsProvider)
        super.load()
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
