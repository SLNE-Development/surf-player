package dev.slne.surf.social.core.client.tab.redis

import dev.slne.surf.redis.RedisApi
import dev.slne.surf.social.core.client.SocialClient

val redisLoader = TabRedisLoader()
val redisApi get() = redisLoader.redisApi

/**
 * Tab's view on the shared surf-social Redis connection owned by [SocialClient].
 */
class TabRedisLoader {
    val redisApi: RedisApi get() = SocialClient.redisApi

    /**
     * Registers the tab Redis listeners. Must run before [SocialClient.connectRedis].
     */
    fun register() {
        redisApi.subscribeToEvents(TabRedisEventListener)
    }
}
