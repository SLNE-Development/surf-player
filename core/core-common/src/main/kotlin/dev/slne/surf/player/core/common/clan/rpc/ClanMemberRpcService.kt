package dev.slne.surf.player.core.common.clan.rpc

import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

/** Clan memberships, stored by the microservice. */
@RpcService
interface ClanMemberRpcService {
    suspend fun createMember(clanId: ULong, player: UUID, role: ClanMemberRole, invitedBy: UUID?): ClanMemberAddResult

    /** @return `false` if [player] is no member of [clanId] */
    suspend fun deleteMember(clanId: ULong, player: UUID): Boolean

    /** @return `false` if there is no member [memberId] */
    suspend fun updateMemberRole(memberId: ULong, role: ClanMemberRole): Boolean
}
