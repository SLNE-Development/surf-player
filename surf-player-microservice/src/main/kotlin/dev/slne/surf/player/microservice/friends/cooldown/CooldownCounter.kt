package dev.slne.surf.player.microservice.friends.cooldown

import java.time.OffsetDateTime

/**
 * How often one [FriendRequestCooldownKind] happened in a row for one direction, each within the
 * escalation window of the one before, and when it happened last.
 */
data class CooldownCounter(
    val count: Int,
    val lastAt: OffsetDateTime?
) {
    companion object {
        val NONE = CooldownCounter(count = 0, lastAt = null)
    }
}
