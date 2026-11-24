package dev.slne.surf.social.microservice.clan.services

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.clan.player.ClanPlayerService
import dev.slne.surf.social.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.social.core.common.clan.player.CoreClanPlayerService
import java.util.*

@AutoService(ClanPlayerService::class)
class ClanPlayerServiceImpl : CoreClanPlayerService {
    override suspend fun invalidateCaches() = Unit

    override suspend fun findByUuid(uuid: UUID): ClanPlayerImpl {
        throw NotImplementedError()
    }

    override suspend fun changeAcceptsClanInvites(
        playerImpl: ClanPlayerImpl,
        acceptsClanInvites: Boolean
    ): Boolean {
        throw NotImplementedError()
    }
}