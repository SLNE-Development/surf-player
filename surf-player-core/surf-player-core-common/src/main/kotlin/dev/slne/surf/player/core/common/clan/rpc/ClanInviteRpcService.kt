package dev.slne.surf.player.core.common.clan.rpc

import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

@RpcService
interface ClanInviteRpcService {

    suspend fun acceptInvite(inviteID: ULong, invitee: UUID, invitedBy: UUID): Boolean
    suspend fun createInvite(clanID: ULong, invitee: UUID, invitedBy: UUID): ClanInviteResult
    suspend fun deleteInvite(clanID: ULong, invitee: UUID): Boolean

    suspend fun findPendingInvitesByClanId(clanID: ULong): Set<ClanInviteImpl>
    suspend fun findPendingInvitesByInvited(invited: UUID): List<ClanInviteImpl>
    suspend fun findPendingInviteByClanNameAndInvited(clanName: String, invited: UUID): ClanInviteImpl?
}