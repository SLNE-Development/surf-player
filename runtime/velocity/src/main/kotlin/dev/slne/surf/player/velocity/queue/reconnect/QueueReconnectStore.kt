package dev.slne.surf.player.velocity.queue.reconnect

import dev.slne.surf.player.core.common.queue.redis.queueRedisKey
import dev.slne.surf.player.core.common.queue.redis.redisApi
import dev.slne.surf.redis.codec.UUIDCodec
import dev.slne.surf.redis.libs.redisson.client.codec.StringCodec
import dev.slne.surf.redis.libs.redisson.codec.CompositeCodec
import kotlinx.coroutines.future.await
import java.util.*
import java.util.concurrent.TimeUnit

object QueueReconnectStore {

    private val reconnects by lazy {
        redisApi.redisson.getMapCache<UUID, String>(
            queueRedisKey("reconnects"),
            CompositeCodec(
                UUIDCodec.INSTANCE,
                StringCodec.INSTANCE,
            ),
        )
    }

    suspend fun put(
        uuid: UUID,
        serverName: String,
        timeoutSeconds: Long,
    ) {
        reconnects.fastPutAsync(
            uuid,
            serverName,
            timeoutSeconds.coerceAtLeast(1),
            TimeUnit.SECONDS,
        ).await()
    }

    suspend fun peek(uuid: UUID): String? {
        return reconnects.getAsync(uuid).await()
    }

    suspend fun remove(uuid: UUID) {
        reconnects.fastRemoveAsync(uuid).await()
    }
}
