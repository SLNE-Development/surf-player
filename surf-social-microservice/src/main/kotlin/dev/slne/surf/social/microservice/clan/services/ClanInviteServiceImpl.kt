package dev.slne.surf.social.microservice.clan.services

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.clan.invite.ClanInvite
import dev.slne.surf.social.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.social.api.clan.invite.ClanInviteResult
import dev.slne.surf.social.api.clan.invite.ClanInviteService
import dev.slne.surf.social.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.social.core.common.clan.invite.CoreClanInviteService
import java.util.*

@AutoService(ClanInviteService::class)
class ClanInviteServiceImpl : CoreClanInviteService {
    override suspend fun fetchPendingInvites(clanID: ULong): Set<ClanInviteImpl> {
        throw NotImplementedError()
    }

    override suspend fun getPendingInviteByPlayerAndClanName(
        invited: UUID,
        clanName: String
    ): ClanInvite? {
        throw NotImplementedError()
    }

    override suspend fun getPendingInvitesByPlayer(invited: UUID): List<ClanInvite> {
        throw NotImplementedError()
    }

    override suspend fun createInvite(
        clanID: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult {
        throw NotImplementedError()
    }

    override suspend fun deleteInvite(clanID: ULong, invitee: UUID): Boolean {
        throw NotImplementedError()
    }

    override suspend fun revokeInvite(invite: ClanInviteImpl): Boolean {
        throw NotImplementedError()
    }

    override suspend fun acceptInvite(invite: ClanInviteImpl): ClanInviteAcceptResult {
        throw NotImplementedError()
    }
}