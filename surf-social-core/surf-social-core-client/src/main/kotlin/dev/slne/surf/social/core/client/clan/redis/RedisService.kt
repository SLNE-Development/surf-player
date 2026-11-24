package dev.slne.surf.social.core.client.clan.redis

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.social.core.common.clan.clan.CoreClanService
import dev.slne.surf.social.core.common.clan.player.CoreClanPlayerService
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.MustBeInvokedByOverriders

abstract class RedisService {
    val redisApi = RedisApi.create()

    suspend fun connect() {
        register()

        withContext(Dispatchers.IO) {
            redisApi.freezeAndConnect()
        }
    }

    @MustBeInvokedByOverriders
    open fun register() {
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            redisApi.disconnect()
        }
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