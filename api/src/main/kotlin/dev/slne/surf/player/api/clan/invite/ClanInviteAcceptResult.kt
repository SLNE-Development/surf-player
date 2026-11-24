package dev.slne.surf.player.api.clan.invite

import dev.slne.surf.player.api.clan.clan.Clan

/**
 * Represents the result of attempting to accept a clan invitation.
 *
 * @see ClanInvite.accept
 */
sealed interface ClanInviteAcceptResult {
    /**
     * Indicates that the invitation could not be accepted because the player
     * is already a member of a clan.
     */
    data object AlreadyInClan : ClanInviteAcceptResult

    /**
     * Indicates that the invitation no longer exists: it was revoked, already accepted or its clan
     * was deleted.
     */
    data object InviteNotFound : ClanInviteAcceptResult

    /**
     * Indicates that the invitation was successfully accepted and the player
     * has joined the clan.
     *
     * @property clan the clan that the player has joined
     */
    data class Accepted(val clan: Clan) : ClanInviteAcceptResult
}