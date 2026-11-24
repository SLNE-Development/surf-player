package dev.slne.surf.player.core.client.clan.services

import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.core.client.clan.rpc.clanInviteRpcService
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import java.util.*

/**
 * Clan invites as seen by a client, loaded from the microservice.
 */
object ClientClanInviteService {
    suspend fun fetchPendingInvites(clanID: ULong): Set<ClanInviteImpl> {
        return clanInviteRpcService.findPendingInvitesByClanId(clanID)
    }

    suspend fun getPendingInviteByPlayerAndClanName(
        invited: UUID,
        clanName: String
    ): ClanInvite? {
        return clanInviteRpcService.findPendingInviteByClanNameAndInvited(clanName, invited)
    }

    suspend fun getPendingInvitesByPlayer(invited: UUID): List<ClanInvite> {
        return clanInviteRpcService.findPendingInvitesByInvited(invited)
    }

    suspend fun createInvite(
        clanID: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult {
        return clanInviteRpcService.createInvite(clanID, invitee, invitedBy)
    }

    suspend fun deleteInvite(clanID: ULong, invitee: UUID): Boolean {
        return clanInviteRpcService.deleteInvite(clanID, invitee)
    }

    suspend fun revokeInvite(invite: ClanInviteImpl): Boolean {
        return deleteInvite(invite.clanID, invite.invited)
    }

    suspend fun acceptInvite(invite: ClanInviteImpl): ClanInviteAcceptResult {
        val accepted = clanInviteRpcService.acceptInvite(invite.id, invite.invited, invite.invitedBy)

        if (!accepted) {
            return ClanInviteAcceptResult.AlreadyInClan
        } else {
            ClientClanService.invalidateCachedClanByID(invite.clanID)

            val clan = ClientClanService.findClanByID(invite.clanID) ?: error("Clan not found")
            return ClanInviteAcceptResult.Accepted(clan)
        }
    }
}
