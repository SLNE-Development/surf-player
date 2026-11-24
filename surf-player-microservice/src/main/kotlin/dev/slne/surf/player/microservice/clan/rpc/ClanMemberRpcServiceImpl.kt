package dev.slne.surf.player.microservice.clan.rpc

import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.player.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.player.microservice.clan.db.repository.ClanMemberRepository
import java.util.*

object ClanMemberRpcServiceImpl : ClanMemberRpcService {
    override suspend fun createMember(
        clanID: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult {
        return ClanMemberRepository.createMember(
            clanID = clanID,
            player = player,
            role = role,
            invitedBy = invitedBy
        )
    }

    override suspend fun deleteMember(clanID: ULong, player: UUID): Boolean {
        return ClanMemberRepository.deleteMember(clanID, player)
    }

    override suspend fun changeMemberRole(
        memberID: ULong,
        role: ClanMemberRole
    ): Boolean {
        return ClanMemberRepository.changeRole(memberID, role)
    }

    override suspend fun findMemberByUUID(uuid: UUID): ClanMemberImpl? {
        return ClanMemberRepository.findByUuid(uuid)
    }
}