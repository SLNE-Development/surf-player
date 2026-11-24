package dev.slne.surf.social.core.client.tab.redis

import dev.slne.surf.social.core.client.SocialClient

val redisLoader = TabRedisLoader()

/**
 * Registers tab's listeners on the shared surf-social Redis connection owned by [SocialClient].
 */
class TabRedisLoader {

    /**
     * Registers the tab Redis listeners. Must run before [SocialClient.connectRedis].
     */
    fun register() {
        SocialClient.redisApi.subscribeToEvents(TabRedisEventListener)
    }
}
