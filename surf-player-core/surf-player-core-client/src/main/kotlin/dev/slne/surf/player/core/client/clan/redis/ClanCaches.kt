package dev.slne.surf.player.core.client.clan.redis

import dev.slne.surf.player.core.common.clan.clan.CoreClanService
import dev.slne.surf.player.core.common.clan.player.CoreClanPlayerService
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * The clan caches kept on the shared surf-player Redis connection.
 */
object ClanCaches {
    private const val NAMESPACE = "surf-clan"

    /** The Redis key of the clan cache called [name]. */
    fun key(name: String) = "$NAMESPACE:$name"

    suspend fun invalidateAll() {
        supervisorScope {
            launch { CoreClanService.invalidateCaches() }
            launch { CoreClanPlayerService.invalidateCaches() }
        }
    }
}
