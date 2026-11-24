package dev.slne.surf.social.core.common.clan.rpc

import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

@RpcService
interface ClanMemberRpcService {

    suspend fun createMember(clanID: ULong, player: UUID, role: ClanMemberRole, invitedBy: UUID?): ClanMemberAddResult
    suspend fun deleteMember(clanID: ULong, player: UUID): Boolean

    suspend fun changeMemberRole(memberID: ULong, role: ClanMemberRole): Boolean

    suspend fun findMemberByUUID(uuid: UUID): ClanMemberImpl?
}