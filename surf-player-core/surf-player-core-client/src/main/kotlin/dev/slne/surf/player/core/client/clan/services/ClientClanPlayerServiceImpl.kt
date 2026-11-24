package dev.slne.surf.player.core.client.clan.services

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.clan.player.ClanPlayer
import dev.slne.surf.player.api.clan.player.ClanPlayerService
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.clan.redis.ClanCaches
import dev.slne.surf.player.core.client.clan.rpc.clanPlayerRpcService
import dev.slne.surf.player.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.player.core.common.clan.player.CoreClanPlayerService
import java.util.*
import kotlin.time.Duration.Companion.minutes

@AutoService(ClanPlayerService::class)
class ClientClanPlayerServiceImpl : CoreClanPlayerService {
    private val cache = SurfPlayerClient.redisApi.createSimpleCache<UUID, ClanPlayerImpl>(
        ClanCaches.key("clan_player_cache"),
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