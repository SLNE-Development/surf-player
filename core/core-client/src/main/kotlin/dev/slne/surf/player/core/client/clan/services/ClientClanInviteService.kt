package dev.slne.surf.player.core.client.clan.services

import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.core.client.clan.rpc.clanInviteRpcService
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.rpc.ClanInviteAcceptance
import java.util.*

/**
 * Clan invites as seen by a client, loaded from the microservice.
 */
object ClientClanInviteService {
    suspend fun fetchPendingInvites(clanID: ULong): Set<ClanInviteImpl> {
        return clanInviteRpcService.findPendingInvitesOfClan(clanID)
    }

    suspend fun getPendingInviteByPlayerAndClanName(
        invited: UUID,
        clanName: String
    ): ClanInvite? {
        return clanInviteRpcService.findPendingInvite(invited, clanName)
    }

    suspend fun getPendingInvitesByPlayer(invited: UUID): List<ClanInvite> {
        return clanInviteRpcService.findPendingInvitesOfPlayer(invited)
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

    /**
     * Accepts [invite]. The API has no result for an invite that no longer exists, so that case is
     * reported as [ClanInviteAcceptResult.AlreadyInClan], as before.
     */
    suspend fun acceptInvite(invite: ClanInviteImpl): ClanInviteAcceptResult {
        return when (val acceptance = clanInviteRpcService.acceptInvite(invite.id, invite.invited, invite.invitedBy)) {
            is ClanInviteAcceptance.Accepted -> {
                ClientClanService.invalidateCachedClanByID(invite.clanID)
                ClanInviteAcceptResult.Accepted(acceptance.clan)
            }

            ClanInviteAcceptance.AlreadyInClan,
            ClanInviteAcceptance.InviteNotFound -> ClanInviteAcceptResult.AlreadyInClan
        }
    }
}
