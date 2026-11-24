package dev.slne.surf.player.core.common.clan.rpc

import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import kotlinx.serialization.Serializable

/** The outcome of [ClanInviteRpcService.acceptInvite]. */
@Serializable
sealed interface ClanInviteAcceptance {
    /** The invitee joined [clan]; [clan] already lists them as member. */
    @Serializable
    data class Accepted(val clan: ClanImpl) : ClanInviteAcceptance

    /** The invitee already is a member of a clan. */
    @Serializable
    data object AlreadyInClan : ClanInviteAcceptance

    /** The invite does not exist (anymore), e.g. because it was revoked. */
    @Serializable
    data object InviteNotFound : ClanInviteAcceptance
}
