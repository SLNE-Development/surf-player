package dev.slne.surf.social.microservice.friends.cooldown

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * @property declineCooldown cooldown after the first decline
 * @property withdrawCooldown cooldown after the first withdrawal
 * @property escalationWindow a further decline (withdrawal) within this time after the previous
 * one doubles the cooldown of that kind; after a longer pause it starts at its base again
 * @property maxCooldown no cooldown is ever longer than this
 */
data class FriendRequestCooldownSettings(
    val declineCooldown: Duration,
    val withdrawCooldown: Duration,
    val escalationWindow: Duration,
    val maxCooldown: Duration
) {
    fun baseCooldown(kind: FriendRequestCooldownKind): Duration = when (kind) {
        FriendRequestCooldownKind.DECLINE -> declineCooldown
        FriendRequestCooldownKind.WITHDRAW -> withdrawCooldown
    }

    companion object {
        val DEFAULT = FriendRequestCooldownSettings(
            declineCooldown = 24.hours,
            withdrawCooldown = 30.minutes,
            escalationWindow = 30.days,
            maxCooldown = 7.days
        )
    }
}
