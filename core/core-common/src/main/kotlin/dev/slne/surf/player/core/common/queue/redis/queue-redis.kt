package dev.slne.surf.player.core.common.queue.redis

import dev.slne.surf.player.core.common.queue.QueueSystem
import dev.slne.surf.redis.RedisApi

/**
 * The Redis connection the queues are stored on: the connection shared by every surf-player
 * feature of the platform.
 */
val redisApi: RedisApi get() = QueueSystem.redisApi

/**
 * Prefixes [key] with the queue namespace. Unchanged since surf-queue, so queues stored by older
 * versions are picked up.
 */
fun queueRedisKey(key: String) = "surf-queue:$key"
