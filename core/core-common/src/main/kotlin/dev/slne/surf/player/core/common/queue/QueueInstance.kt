package dev.slne.surf.player.core.common.queue

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.core.common.queue.queue.AbstractQueue
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService
import dev.slne.surf.player.core.common.queue.queue.tick.QueueScheduler
import dev.slne.surf.redis.RedisApi
import org.jetbrains.annotations.MustBeInvokedByOverriders

/**
 * Bootstraps the queue system of one platform.
 *
 * Platform-specific subclasses supply the shared [redisApi] and implement [createQueue] to return
 * the appropriate [AbstractQueue] variant. The platform's queue feature drives the lifecycle:
 * [enable] runs once Redis is connected, [disable] before it disconnects.
 */
abstract class QueueInstance {
    /** The Redis connection shared by every surf-player feature of the platform. */
    abstract val redisApi: RedisApi
    abstract val queueScheduler: QueueScheduler
    abstract val isLoaded: Boolean

    /**
     * Starts fetching the existing queues from Redis. Subclasses **must** call `super.enable()`.
     */
    @MustBeInvokedByOverriders
    open suspend fun enable() {
        RedisQueueService.startRefreshing()
    }

    /**
     * Closes all queues and stops their ticking. Subclasses **must** call `super.disable()`.
     */
    @MustBeInvokedByOverriders
    open suspend fun disable() {
        RedisQueueService.close()
        queueScheduler.close()
    }

    /**
     * Factory method to create a new [AbstractQueue] for the given [serverName].
     *
     * @param serverName the name of the target server
     * @return a platform-specific queue implementation
     */
    abstract fun createQueue(serverName: String): AbstractQueue

    companion object {
        val instance = requiredService<QueueInstance>()
        fun get() = instance
    }
}
