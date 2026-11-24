package dev.slne.surf.player.core.common.clan.rpc

import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

/** Pending clan invites, stored by the microservice. */
@RpcService
interface ClanInviteRpcService {
    /** Deletes the invite [inviteId] of [invitee] and adds them to its clan in one transaction. */
    suspend fun acceptInvite(inviteId: ULong, invitee: UUID, invitedBy: UUID): ClanInviteAcceptance

    suspend fun createInvite(clanId: ULong, invitee: UUID, invitedBy: UUID): ClanInviteResult

    /** @return `false` if [invitee] had no pending invite of [clanId] */
    suspend fun deleteInvite(clanId: ULong, invitee: UUID): Boolean

    suspend fun findPendingInvitesOfClan(clanId: ULong): Set<ClanInviteImpl>
    suspend fun findPendingInvitesOfPlayer(invitee: UUID): List<ClanInviteImpl>

    /** The pending invite of [invitee] to the clan named [clanName], or `null` if there is none. */
    suspend fun findPendingInvite(invitee: UUID, clanName: String): ClanInviteImpl?
}
