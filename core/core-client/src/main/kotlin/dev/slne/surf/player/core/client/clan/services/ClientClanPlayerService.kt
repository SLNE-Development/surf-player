package dev.slne.surf.player.core.client.clan.services

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.clan.redis.ClanCaches
import dev.slne.surf.player.core.client.clan.rpc.clanPlayerRpcService
import dev.slne.surf.player.core.common.clan.player.ClanPlayerImpl
import java.util.*
import kotlin.time.Duration.Companion.minutes

/**
 * The clan related data of players (e.g. whether they accept clan invites), cached in Redis.
 */
object ClientClanPlayerService {
    private val cache = SurfPlayerClient.redisApi.createSimpleCache<UUID, ClanPlayerImpl>(
        ClanCaches.key("clan_player_cache"),
        30.minutes
    )

    /** Creates the clan player cache. Must run before the Redis connection is established. */
    fun init() = Unit

    suspend fun invalidateCaches() {
        cache.invalidateAll()
    }

    suspend fun findByUuid(uuid: UUID): ClanPlayerImpl {
        return cache.cachedOrLoad(uuid) {
            clanPlayerRpcService.findClanPlayerByUuid(uuid)
        }
    }

    suspend fun changeAcceptsClanInvites(uuid: UUID, acceptsClanInvites: Boolean): Boolean {
        val player = findByUuid(uuid)
        val changed = clanPlayerRpcService.updateAcceptsClanInvites(player.ID, acceptsClanInvites)

        if (changed) {
            cache.invalidate(player.uuid)
        }

        return changed
    }
}
