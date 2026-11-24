package dev.slne.surf.player.microservice.clan.rpc

import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.rpc.ClanInviteAcceptance
import dev.slne.surf.player.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.player.microservice.clan.db.repository.ClanInviteRepository
import java.util.*

object ClanInviteRpcServiceImpl : ClanInviteRpcService {
    override suspend fun acceptInvite(
        inviteId: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteAcceptance {
        return ClanInviteRepository.acceptInvite(
            inviteID = inviteId,
            invitee = invitee,
            invitedBy = invitedBy
        )
    }

    override suspend fun createInvite(
        clanId: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult {
        return ClanInviteRepository.createInvite(
            clanID = clanId,
            invitee = invitee,
            invitedBy = invitedBy
        )
    }

    override suspend fun deleteInvite(clanId: ULong, invitee: UUID): Boolean {
        return ClanInviteRepository.deleteInvite(clanId, invitee)
    }

    override suspend fun findPendingInvitesOfClan(clanId: ULong): Set<ClanInviteImpl> {
        return ClanInviteRepository.fetchPendingInvites(clanId)
    }

    override suspend fun findPendingInvitesOfPlayer(invitee: UUID): List<ClanInviteImpl> {
        return ClanInviteRepository.getPendingInvitesByPlayer(invitee)
    }

    override suspend fun findPendingInvite(invitee: UUID, clanName: String): ClanInviteImpl? {
        return ClanInviteRepository.getPendingInviteByPlayerAndClanName(
            invited = invitee,
            clanName = clanName
        )
    }
}
