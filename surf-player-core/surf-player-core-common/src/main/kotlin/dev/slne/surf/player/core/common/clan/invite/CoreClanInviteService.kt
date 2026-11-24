package dev.slne.surf.player.core.common.clan.invite

import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.invite.ClanInviteService
import java.util.*

interface CoreClanInviteService : ClanInviteService {

    suspend fun fetchPendingInvites(clanID: ULong): Set<ClanInviteImpl>
    suspend fun createInvite(clanID: ULong, invitee: UUID, invitedBy: UUID): ClanInviteResult
    suspend fun deleteInvite(clanID: ULong, invitee: UUID): Boolean

    suspend fun acceptInvite(invite: ClanInviteImpl): ClanInviteAcceptResult
    suspend fun revokeInvite(invite: ClanInviteImpl): Boolean

    companion object : CoreClanInviteService by ClanInviteService.INSTANCE as CoreClanInviteService
}

