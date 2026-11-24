package dev.slne.surf.player.microservice.clan.rpc

import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.player.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.player.microservice.clan.db.repository.ClanMemberRepository
import java.util.*

object ClanMemberRpcServiceImpl : ClanMemberRpcService {
    override suspend fun createMember(
        clanId: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult {
        return ClanMemberRepository.createMember(
            clanID = clanId,
            player = player,
            role = role,
            invitedBy = invitedBy
        )
    }

    override suspend fun deleteMember(clanId: ULong, player: UUID): Boolean {
        return ClanMemberRepository.deleteMember(clanId, player)
    }

    override suspend fun updateMemberRole(
        memberId: ULong,
        role: ClanMemberRole
    ): Boolean {
        return ClanMemberRepository.changeRole(memberId, role)
    }

    override suspend fun findMemberByUuid(uuid: UUID): ClanMemberImpl? {
        return ClanMemberRepository.findByUuid(uuid)
    }
}
