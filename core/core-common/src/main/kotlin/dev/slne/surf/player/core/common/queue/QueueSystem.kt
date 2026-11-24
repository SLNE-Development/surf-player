package dev.slne.surf.player.core.common.queue

import dev.slne.surf.player.core.common.queue.queue.AbstractQueue
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService
import dev.slne.surf.player.core.common.queue.queue.tick.QueueScheduler
import dev.slne.surf.redis.RedisApi

/**
 * The queue system of one platform.
 *
 * The platform's queue feature drives the lifecycle: [init] while loading (before anything touches
 * a queue), [enable] once Redis is connected and [disable] before it disconnects.
 */
object QueueSystem {
    private lateinit var redisApiProvider: () -> RedisApi
    private lateinit var queueFactory: (serverName: String) -> AbstractQueue

    /** The Redis connection shared by every surf-player feature of the platform. */
    val redisApi: RedisApi get() = redisApiProvider()

    /** Runs the ticking of the queues; set by [init]. */
    lateinit var queueScheduler: QueueScheduler
        private set

    /**
     * Whether this server accepts players, so queued players may be sent here. Set by the
     * platform's queue feature.
     */
    @Volatile
    var isLoaded: Boolean = false

    /**
     * Sets up the queue system of the platform.
     *
     * @param redisApi resolves the shared Redis connection; called lazily
     * @param queueScheduler runs the ticking of the queues
     * @param queueFactory creates the platform-specific queue of a server
     */
    fun init(
        redisApi: () -> RedisApi,
        queueScheduler: QueueScheduler,
        queueFactory: (serverName: String) -> AbstractQueue,
    ) {
        this.redisApiProvider = redisApi
        this.queueScheduler = queueScheduler
        this.queueFactory = queueFactory
    }

    /** Creates the platform-specific queue for the server named [serverName]. */
    fun createQueue(serverName: String): AbstractQueue = queueFactory(serverName)

    /** Starts fetching the existing queues from Redis. */
    fun enable() {
        RedisQueueService.startRefreshing()
    }

    /** Closes all queues and stops their ticking. */
    fun disable() {
        RedisQueueService.close()
        queueScheduler.close()
    }
}
