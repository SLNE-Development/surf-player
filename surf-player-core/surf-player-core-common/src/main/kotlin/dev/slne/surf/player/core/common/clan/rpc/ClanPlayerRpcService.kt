package dev.slne.surf.player.core.common.clan.rpc

import dev.slne.surf.player.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

@RpcService
interface ClanPlayerRpcService {

    suspend fun findClanPlayerByUuid(uuid: UUID): ClanPlayerImpl
    suspend fun updateAcceptsClanInvites(playerID: ULong, acceptsClanInvites: Boolean): Boolean

}