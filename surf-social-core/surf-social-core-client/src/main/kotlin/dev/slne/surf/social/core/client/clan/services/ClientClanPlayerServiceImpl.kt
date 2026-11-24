package dev.slne.surf.social.core.client.clan.services

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.clan.player.ClanPlayer
import dev.slne.surf.social.api.clan.player.ClanPlayerService
import dev.slne.surf.social.core.client.clan.redis.RedisService
import dev.slne.surf.social.core.client.clan.rpc.clanPlayerRpcService
import dev.slne.surf.social.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.social.core.common.clan.player.CoreClanPlayerService
import java.util.*
import kotlin.time.Duration.Companion.minutes

@AutoService(ClanPlayerService::class)
class ClientClanPlayerServiceImpl : CoreClanPlayerService {
    private val cache = RedisService.get().redisApi.createSimpleCache<UUID, ClanPlayerImpl>(
        RedisService.namespaced("clan_player_cache"),
        30.minutes
    )

    override suspend fun invalidateCaches() {
        cache.invalidateAll()
    }

    override suspend fun findByUuid(uuid: UUID): ClanPlayer {
        return cache.cachedOrLoad(uuid) {
            clanPlayerRpcService.findClanPlayerByUuid(uuid)
        }
    }

    override suspend fun changeAcceptsClanInvites(
        playerImpl: ClanPlayerImpl,
        acceptsClanInvites: Boolean
    ): Boolean {
        val changed = clanPlayerRpcService.updateAcceptsClanInvites(playerImpl.ID, acceptsClanInvites)

        if (changed) {
            cache.invalidate(playerImpl.uuid)
        }

        return changed
    }
}