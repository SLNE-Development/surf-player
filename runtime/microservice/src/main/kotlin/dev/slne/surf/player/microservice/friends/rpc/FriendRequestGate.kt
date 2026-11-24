package dev.slne.surf.player.microservice.friends.rpc

import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.core.common.friends.rpc.FriendRequestPrivileges
import java.util.*
import kotlin.time.Duration

/**
 * The checks the microservice runs before storing a friend request, in this order: ignores
 * ([FriendRequestCreateResult.Blocked]), the target's setting `friend:requests`
 * ([FriendRequestCreateResult.RequestsDisabled]) and cooldowns
 * ([FriendRequestCreateResult.OnCooldown]). Each lookup runs only when the check before passed.
 */
object FriendRequestGate {
    /**
     * Why the request from the sender to [targetUuid] must not be stored, or `null` if it may be.
     *
     * - The sender ignoring the target always blocks. The target ignoring the sender does not
     *   block a team member ([FriendRequestPrivileges.ignoreExempt]).
     * - A sender with [FriendRequestPrivileges.cooldownBypass] is never on cooldown.
     */
    suspend fun refusal(
        targetUuid: UUID,
        privileges: FriendRequestPrivileges,
        senderIgnoresTarget: suspend () -> Boolean,
        targetIgnoresSender: suspend () -> Boolean,
        targetAcceptsRequests: suspend () -> Boolean,
        remainingCooldown: suspend () -> Duration?,
    ): FriendRequestCreateResult? {
        if (senderIgnoresTarget() || (!privileges.ignoreExempt && targetIgnoresSender())) {
            return FriendRequestCreateResult.Blocked(targetUuid)
        }

        if (!targetAcceptsRequests()) {
            return FriendRequestCreateResult.RequestsDisabled(targetUuid)
        }

        if (!privileges.cooldownBypass) {
            remainingCooldown()?.let { return FriendRequestCreateResult.OnCooldown(targetUuid, it) }
        }

        return null
    }
}
