package dev.slne.surf.player.microservice.clan.rpc

import dev.slne.surf.player.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.player.core.common.clan.rpc.ClanPlayerRpcService
import dev.slne.surf.player.microservice.clan.db.repository.ClanPlayerRepository
import java.util.*

object ClanPlayerRpcServiceImpl : ClanPlayerRpcService {

    override suspend fun findClanPlayerByUuid(uuid: UUID): ClanPlayerImpl {
        return ClanPlayerRepository.findOrCreateByUuid(uuid)
    }

    override suspend fun updateAcceptsClanInvites(
        playerID: ULong,
        acceptsClanInvites: Boolean
    ): Boolean {
        return ClanPlayerRepository.changeAcceptsClanInvites(playerID, acceptsClanInvites)
    }
}