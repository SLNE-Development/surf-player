package dev.slne.surf.player.core.client.clan.redis

import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.core.common.playerRedisKey

/**
 * The clan caches kept on the shared surf-player Redis connection.
 */
object ClanCaches {
    /** The Redis key of the clan cache called [name]. */
    fun key(name: String) = playerRedisKey("clan", name)

    suspend fun invalidateAll() {
        ClientClanService.invalidateCaches()
    }
}
