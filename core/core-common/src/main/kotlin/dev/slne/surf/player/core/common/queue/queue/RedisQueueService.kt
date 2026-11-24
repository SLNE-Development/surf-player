package dev.slne.surf.player.core.common.queue.queue

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.runAtFixedRate
import dev.slne.surf.api.core.util.withAutoCloseOnRemoval
import dev.slne.surf.player.api.queue.InternalSurfQueueApi
import dev.slne.surf.player.api.queue.service.SurfQueueService
import dev.slne.surf.player.core.common.queue.QueueInstance
import dev.slne.surf.player.core.common.queue.redis.redisApi
import dev.slne.surf.redis.libs.redisson.api.options.KeysScanOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.reactive.collect
import java.lang.AutoCloseable
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * Creates, caches and periodically discovers the queues stored in Redis.
 */
@OptIn(InternalSurfQueueApi::class)
object RedisQueueService : SurfQueueService, AutoCloseable {
    const val QUEUE_REFRESH_INTERVAL_SECONDS = 30
    private val log = logger()

    private val queues = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(QUEUE_REFRESH_INTERVAL_SECONDS * 4L))
        .withAutoCloseOnRemoval { key, _, cause ->
            log.atInfo().log("Queue removed from cache: key=%s, cause=%s", key, cause)
        }
        .build<String, AbstractQueue> { serverName ->
            QueueInstance.get().createQueue(serverName).also { queue ->
                if (queue is AbstractTickableQueue) {
                    queue.startTicking()
                }
            }
        }

    private val scope = QueueInstance.get().queueScheduler.createServiceScope()
    private var refreshJob: Job? = null

    override fun getQueueByName(serverName: String): AbstractQueue = queues.get(serverName)
    fun getAll(): Collection<AbstractQueue> = queues.asMap().values

    fun delete(serverName: String) = queues.invalidate(serverName)

    suspend fun fetchFromRedis() {
        redisApi.redissonReactive.keys
            .getKeys(
                KeysScanOptions.defaults()
                    .pattern(RedisQueueKeys.EPOCH_MS_KEY_PATTERN)
            ).map(::extractServerName)
            .collect(::getQueueByName)
    }

    fun startRefreshing() {
        require(refreshJob == null) { "Refresh job already started" }
        refreshJob = scope.runAtFixedRate(QUEUE_REFRESH_INTERVAL_SECONDS.seconds) {
            try {
                fetchFromRedis()
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                log.atWarning()
                    .withCause(e)
                    .log("Failed to refresh queues from Redis")
            }
        }
    }

    private fun extractServerName(key: String) = key
        .replaceFirst(RedisQueueKeys.QUEUE_PREFIX, "")
        .replaceFirst(RedisQueueKeys.EPOCH_MS_SUFFIX, "")

    override fun close() {
        scope.cancel("RedisQueueService closed")
        queues.invalidateAll()
    }
}
