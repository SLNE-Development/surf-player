package dev.slne.surf.social.microservice.clan.rpc

import dev.slne.surf.social.api.clan.invite.ClanInviteResult
import dev.slne.surf.social.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.social.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.social.microservice.clan.db.repository.ClanInviteRepository
import java.util.*

object ClanInviteRpcServiceImpl : ClanInviteRpcService {
    override suspend fun acceptInvite(
        inviteID: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): Boolean {
        return ClanInviteRepository.acceptInvite(
            inviteID = inviteID,
            invitee = invitee,
            invitedBy = invitedBy
        )
    }

    override suspend fun createInvite(
        clanID: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult {
        return ClanInviteRepository.createInvite(
            clanID = clanID,
            invitee = invitee,
            invitedBy = invitedBy
        )
    }

    override suspend fun deleteInvite(clanID: ULong, invitee: UUID): Boolean {
        return ClanInviteRepository.deleteInvite(clanID, invitee)
    }

    override suspend fun findPendingInvitesByClanId(clanID: ULong): Set<ClanInviteImpl> {
        return ClanInviteRepository.fetchPendingInvites(clanID)
    }

    override suspend fun findPendingInvitesByInvited(invited: UUID): List<ClanInviteImpl> {
        return ClanInviteRepository.getPendingInvitesByPlayer(invited)
    }

    override suspend fun findPendingInviteByClanNameAndInvited(
        clanName: String,
        invited: UUID
    ): ClanInviteImpl? {
        return ClanInviteRepository.getPendingInviteByPlayerAndClanName(
            invited = invited,
            clanName = clanName
        )
    }
}