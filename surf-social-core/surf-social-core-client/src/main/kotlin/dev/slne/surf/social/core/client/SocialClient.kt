package dev.slne.surf.social.core.client

import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.social.core.common.SOCIAL_CONNECTION_NAME
import dev.slne.surf.social.core.common.SocialSerializerModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * Owns the single RabbitMQ and Redis connection that every surf-social feature on a client
 * platform (Paper, Minestom, Velocity) shares.
 *
 * Lifecycle, driven by the platform plugin:
 * 1. [load] once, before any feature is loaded (creates both APIs).
 * 2. [connectRabbit] once, right after [load]. Clients register nothing on RabbitMQ before
 *    freezing, so features may send requests from their own load phase onwards.
 * 3. Features load and register everything Redis needs before freezing (event listeners,
 *    request handlers, sync structures, event types).
 * 4. [connectRedis] once, after every feature has loaded and before any feature is enabled.
 * 5. [disable] once, after every feature has been disabled.
 *
 * [connectRabbit], [connectRedis] and [disable] are idempotent.
 */
object SocialClient {
    @Volatile
    private var rabbit: ClientRabbitMQApi? = null

    @Volatile
    private var redis: RedisApi? = null

    private var rabbitConnected = false
    private var redisConnected = false
    private var disabled = false

    val rabbitApi: ClientRabbitMQApi
        get() = rabbit ?: error("SocialClient.load(dataPath) has not been called yet")

    val redisApi: RedisApi
        get() = redis ?: error("SocialClient.load(dataPath) has not been called yet")

    @Synchronized
    fun load(dataPath: Path) {
        if (rabbit != null) return

        rabbit = ClientRabbitMQApi.create(
            SOCIAL_CONNECTION_NAME,
            dataPath,
            SocialSerializerModule.module
        )
        redis = RedisApi.create(SOCIAL_CONNECTION_NAME)
    }

    suspend fun connectRabbit() {
        if (rabbitConnected) return
        rabbitConnected = true
        rabbitApi.freezeAndConnect()
    }

    suspend fun connectRedis() {
        if (redisConnected) return
        redisConnected = true
        withContext(Dispatchers.IO) {
            redisApi.freezeAndConnect()
        }
    }

    /** Convenience for [connectRabbit] followed by [connectRedis]. */
    suspend fun enable() {
        connectRabbit()
        connectRedis()
    }

    suspend fun disable() {
        if (disabled) return
        disabled = true

        if (redisConnected) {
            withContext(Dispatchers.IO) {
                redisApi.disconnect()
            }
        }

        if (rabbitConnected) {
            rabbitApi.disconnect()
        }
    }
}
