package dev.slne.surf.player.core.common.queue.queue

import dev.slne.surf.player.core.common.queue.redis.queueRedisKey

/** The Redis keys of the queue of the server named [serverName]: `surf-player:queue:servers:<server>:<name>`. */
data class RedisQueueKeys(
    val serverName: String,
) {
    val entriesKey = "$QUEUE_PREFIX$serverName:entries"
    val metaKey = "$QUEUE_PREFIX$serverName:meta"
    val retryCountKey = "$QUEUE_PREFIX$serverName:retry-count"
    val lastSeenKey = "$QUEUE_PREFIX$serverName:last-seen"
    val transferSemaphoreKey = "$QUEUE_PREFIX$serverName:transfer-semaphore"
    val cleanupSemaphoreKey = "$QUEUE_PREFIX$serverName:cleanup-semaphore"
    val pausedKey = "$QUEUE_PREFIX$serverName:paused"
    val epochMsKey = "$QUEUE_PREFIX$serverName$EPOCH_MS_SUFFIX"

    companion object {
        val QUEUE_PREFIX = queueRedisKey("servers:")
        val EPOCH_MS_KEY_PATTERN = "$QUEUE_PREFIX*$EPOCH_MS_SUFFIX"
        const val EPOCH_MS_SUFFIX = ":epoch-ms"
    }
}
