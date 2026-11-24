package dev.slne.surf.player.core.client.queue

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.queue.config.SurfQueueConfig
import dev.slne.surf.player.core.client.queue.metrics.QueueMetricsLogger
import dev.slne.surf.player.core.client.queue.queue.ClientQueueImpl
import dev.slne.surf.player.core.client.queue.queue.OwnedClientQueueImpl
import dev.slne.surf.player.core.common.queue.QueueSystem
import dev.slne.surf.player.core.common.queue.queue.AbstractQueue
import dev.slne.surf.player.core.common.queue.queue.tick.QueueScheduler
import java.nio.file.Path

/**
 * The queue system of game servers (Paper and Minestom), which own and process queues.
 *
 * The queue of the server itself ticks transfers and cleanup, while queues of other servers are
 * created as plain, non-ticking queues used for administrative access.
 *
 * The platform's queue feature calls [load], [enable] and [disable]; it sets
 * [QueueSystem.isLoaded] once the server accepts players.
 */
object ClientQueueSystem {

    /**
     * Loads the configuration from [dataPath] and sets up [QueueSystem]. Runs before Redis is
     * connected.
     */
    fun load(dataPath: Path) {
        SurfQueueConfig.init(dataPath)
        QueueSystem.init(
            redisApi = { SurfPlayerClient.redisApi },
            // Only one tickable queue max, so 1 worker is enough
            queueScheduler = QueueScheduler(workerCount = 1),
            queueFactory = ::createQueue,
        )
    }

    fun enable() {
        QueueSystem.enable()
    }

    fun disable() {
        QueueSystem.disable()
        QueueMetricsLogger.stop()
    }

    private fun createQueue(serverName: String): AbstractQueue {
        return if (serverName == SurfServer.current().name) {
            OwnedClientQueueImpl(serverName, QueueSystem.queueScheduler)
        } else {
            ClientQueueImpl(serverName)
        }
    }
}
