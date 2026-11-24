package dev.slne.surf.player.api.clan.member.listener

import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.member.ClanMemberRole

/**
 * Listener for clan member role change events.
 *
 * This functional interface is invoked whenever a clan member's role is changed.
 * As a SAM (Single Abstract Method) interface, it can be implemented using lambda expressions.
 *
 * Example usage:
 * ```kotlin
 * val listener = ClanMemberChangedRoleListener { member, newRole ->
 *     println("${member.uuid} changed to $newRole")
 * }
 * SurfClanApi.registerListener(listener)
 * ```
 *
 * @see ClanMemberListener
 * @see dev.slne.surf.player.api.clan.SurfClanApi.registerListener
 */
fun interface ClanMemberChangedRoleListener : ClanMemberListener {

    /**
     * Called when a clan member's role has been changed.
     *
     * This method is invoked after the role change has been successfully applied.
     *
     * @param member the member whose role was changed
     * @param newRole the newly assigned role
     */
    fun onClanMemberChangedRole(member: ClanMember, newRole: ClanMemberRole)
}