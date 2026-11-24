package dev.slne.surf.social.velocity.connection.redis

import dev.slne.surf.redis.RedisApi
import dev.slne.surf.social.core.client.SocialClient

/**
 * Redis access of the connection feature. Uses the shared surf-social Redis connection owned by
 * [SocialClient]; the publisher is created on first use, after the connection is established.
 */
object SocialRedisService {
    val redisApi: RedisApi get() = SocialClient.redisApi

    val discordRolesPublisher by lazy { DiscordRolesPublisher(redisApi) }
}
