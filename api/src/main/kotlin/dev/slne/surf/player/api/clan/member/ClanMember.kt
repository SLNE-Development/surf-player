package dev.slne.surf.player.api.clan.member

import org.jetbrains.annotations.ApiStatus

/**
 * Represents a mutable clan member with the ability to perform actions.
 *
 * This interface extends [ClanMemberView] and provides methods for modifying member data
 * and managing member-related listeners.
 *
 * @see ClanMemberView
 */
@ApiStatus.NonExtendable
interface ClanMember : ClanMemberView {
    /**
     * Changes the role of this clan member.
     *
     * This operation is performed asynchronously and may involve database updates and
     * permission checks.
     *
     * @param role the new role to assign to this member
     * @return `true` if the role change was successful, `false` otherwise
     * @see ClanMemberRole
     */
    suspend fun changeRole(role: ClanMemberRole): Boolean

    /**
     * Returns a read-only view of this clan member.
     *
     * Use this method when you need to pass member information to code that should not
     * be able to modify the member's data.
     *
     * @return an immutable view of this member
     */
    fun view(): ClanMemberView
}