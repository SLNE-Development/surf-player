package dev.slne.surf.social.microservice.friends.cooldown

import java.time.OffsetDateTime
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/**
 * The cooldown of one direction (sender → receiver): until [expiresAt] the sender may not send the
 * receiver another friend request. Declines and withdrawals escalate independently.
 */
data class FriendRequestCooldown(
    val declines: CooldownCounter,
    val withdrawals: CooldownCounter,
    val expiresAt: OffsetDateTime
) {
    fun counter(kind: FriendRequestCooldownKind): CooldownCounter = when (kind) {
        FriendRequestCooldownKind.DECLINE -> declines
        FriendRequestCooldownKind.WITHDRAW -> withdrawals
    }

    fun withCounter(kind: FriendRequestCooldownKind, counter: CooldownCounter) = when (kind) {
        FriendRequestCooldownKind.DECLINE -> copy(declines = counter)
        FriendRequestCooldownKind.WITHDRAW -> copy(withdrawals = counter)
    }

    /** The time left at [now], or `null` once the cooldown is over. */
    fun remainingAt(now: OffsetDateTime): Duration? =
        java.time.Duration.between(now, expiresAt)
            .takeIf { it.isPositive }
            ?.toKotlinDuration()

    companion object {
        /** No decline or withdrawal recorded yet. */
        val NONE = FriendRequestCooldown(
            declines = CooldownCounter.NONE,
            withdrawals = CooldownCounter.NONE,
            expiresAt = OffsetDateTime.MIN
        )
    }
}
