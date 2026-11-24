package dev.slne.surf.social.core.client.clan.redis

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.common.clan.clan.CoreClanService
import dev.slne.surf.social.core.common.clan.player.CoreClanPlayerService
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.jetbrains.annotations.MustBeInvokedByOverriders

/**
 * Clan's view on the shared surf-social Redis connection owned by [SocialClient].
 * Connecting and disconnecting is done by [SocialClient]; [register] is called during clan load,
 * before the shared connection is frozen.
 */
abstract class RedisService {
    val redisApi: RedisApi get() = SocialClient.redisApi

    @MustBeInvokedByOverriders
    open fun register() {
    }

    suspend fun invalidateAllCaches() {
        supervisorScope {
            launch { CoreClanService.invalidateCaches() }
            launch { CoreClanPlayerService.invalidateCaches() }
        }
    }

    companion object {
        const val NAMESPACE = "surf-clan"
        val instance = requiredService<RedisService>()
        fun get() = instance

        fun namespaced(namespace: String) = "$NAMESPACE:$namespace"
        fun publish(event: RedisEvent) = get().redisApi.publishEvent(event)
    }
}