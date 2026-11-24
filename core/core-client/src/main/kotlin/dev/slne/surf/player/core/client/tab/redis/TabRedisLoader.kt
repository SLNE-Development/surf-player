package dev.slne.surf.player.core.client.tab.redis

import dev.slne.surf.player.core.client.SurfPlayerClient

val redisLoader = TabRedisLoader()

/**
 * Registers tab's listeners on the shared surf-player Redis connection owned by [SurfPlayerClient].
 */
class TabRedisLoader {

    /**
     * Registers the tab Redis listeners. Must run before [SurfPlayerClient.connectRedis].
     */
    fun register() {
        SurfPlayerClient.redisApi.subscribeToEvents(TabRedisEventListener)
    }
}
