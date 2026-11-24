package dev.slne.surf.player.core.common.friends.rpc

import kotlinx.serialization.Serializable

/**
 * What the sender of a friend request may skip. The client sets these from the sender's
 * permissions; the microservice trusts them, since only the network's own servers call it.
 *
 * @property ignoreExempt the sender is a team member (`surf.player.chat.ignore.exempt`): the
 * target ignoring the sender does not block the request
 * @property cooldownBypass the sender has `surf.player.friends.cooldown.bypass`: cooldowns do not
 * apply
 */
@Serializable
data class FriendRequestPrivileges(
    val ignoreExempt: Boolean = false,
    val cooldownBypass: Boolean = false,
) {
    companion object {
        val NONE = FriendRequestPrivileges()
    }
}
