package dev.slne.surf.player.core.common.queue.redis

import dev.slne.surf.player.core.common.playerRedisKey
import dev.slne.surf.player.core.common.queue.QueueSystem
import dev.slne.surf.redis.RedisApi

/**
 * The Redis connection the queues are stored on: the connection shared by every surf-player
 * feature of the platform.
 */
val redisApi: RedisApi get() = QueueSystem.redisApi

/** The Redis key `surf-player:queue:<key>`. */
fun queueRedisKey(key: String) = playerRedisKey("queue", key)
