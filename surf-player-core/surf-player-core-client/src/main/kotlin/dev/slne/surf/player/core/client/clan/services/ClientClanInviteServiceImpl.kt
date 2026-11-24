package dev.slne.surf.player.core.client.clan.services

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.invite.ClanInviteService
import dev.slne.surf.player.core.common.clan.clan.CoreClanService
import dev.slne.surf.player.core.client.clan.rpc.clanInviteRpcService
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.invite.CoreClanInviteService
import java.util.*

@AutoService(ClanInviteService::class)
class ClientClanInviteServiceImpl : CoreClanInviteService {
    override suspend fun fetchPendingInvites(clanID: ULong): Set<ClanInviteImpl> {
        return clanInviteRpcService.findPendingInvitesByClanId(clanID)
    }

    override suspend fun getPendingInviteByPlayerAndClanName(
        invited: UUID,
        clanName: String
    ): ClanInvite? {
        return clanInviteRpcService.findPendingInviteByClanNameAndInvited(clanName, invited)
    }

    override suspend fun getPendingInvitesByPlayer(invited: UUID): List<ClanInvite> {
        return clanInviteRpcService.findPendingInvitesByInvited(invited)
    }

    override suspend fun createInvite(
        clanID: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult {
        return clanInviteRpcService.createInvite(clanID, invitee, invitedBy)
    }

    override suspend fun deleteInvite(clanID: ULong, invitee: UUID): Boolean {
        return clanInviteRpcService.deleteInvite(clanID, invitee)
    }

    override suspend fun revokeInvite(invite: ClanInviteImpl): Boolean {
        return deleteInvite(invite.clanID, invite.invited)
    }

    override suspend fun acceptInvite(invite: ClanInviteImpl): ClanInviteAcceptResult {
        val accepted = clanInviteRpcService.acceptInvite(invite.id, invite.invited, invite.invitedBy)

        if (!accepted) {
            return ClanInviteAcceptResult.AlreadyInClan
        } else {
            ClientClanServiceImpl.get().invalidateCachedClanByID(invite.clanID)

            val clan = CoreClanService.findClanByID(invite.clanID) ?: error("Clan not found")
            return ClanInviteAcceptResult.Accepted(clan)
        }
    }

    companion object {
        fun get() = ClanInviteService.INSTANCE as ClientClanInviteServiceImpl
    }
}