package dev.slne.surf.player.core.client.clan.redis

import dev.slne.surf.player.core.client.clan.services.ClientClanPlayerService
import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.core.common.playerRedisKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * The clan caches kept on the shared surf-player Redis connection.
 */
object ClanCaches {
    /** The Redis key of the clan cache called [name]. */
    fun key(name: String) = playerRedisKey("clan", name)

    suspend fun invalidateAll() {
        supervisorScope {
            launch { ClientClanService.invalidateCaches() }
            launch { ClientClanPlayerService.invalidateCaches() }
        }
    }
}
