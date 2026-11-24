package dev.slne.surf.player.api.clan.clan.listener

import dev.slne.surf.player.api.clan.clan.Clan
import java.util.*

/**
 * Listener for clan member addition and removal events.
 *
 * This functional interface is invoked whenever a member is added to or removed from a clan.
 * As a SAM (Single Abstract Method) interface, it can be implemented using lambda expressions.
 *
 * Example usage:
 * ```kotlin
 * val listener = ClanUpdateMemberListener { clan, memberUuid, added ->
 *     if (added) {
 *         println("Member $memberUuid joined ${clan.name}")
 *     } else {
 *         println("Member $memberUuid left ${clan.name}")
 *     }
 * }
 * SurfClanApi.registerListener(listener)
 * ```
 *
 * @see ClanListener
 * @see dev.slne.surf.player.api.clan.SurfClanApi.registerListener
 */
fun interface ClanUpdateMemberListener : ClanListener {

    /**
     * Called when a member is added to or removed from a clan.
     *
     * This method is invoked after the member change has been successfully applied.
     *
     * @param clan the clan that was modified
     * @param memberUuid the UUID of the member who was added or removed
     * @param added `true` if the member was added, `false` if the member was removed
     */
    fun onClanMemberUpdated(clan: Clan, memberUuid: UUID, added: Boolean)
}