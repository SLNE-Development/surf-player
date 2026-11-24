package dev.slne.surf.player.core.common.clan.rpc

import dev.slne.surf.player.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

/** The clan related data of every player, stored by the microservice. */
@RpcService
interface ClanPlayerRpcService {
    /** The clan data of [uuid]; created with the defaults if the player has none yet. */
    suspend fun findClanPlayer(uuid: UUID): ClanPlayerImpl

    /** Stores whether [uuid] accepts clan invites, creating their clan data if needed. */
    suspend fun updateAcceptsClanInvites(uuid: UUID, acceptsClanInvites: Boolean)
}
