package dev.slne.surf.social.microservice.friends.cooldown

import java.time.OffsetDateTime
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * Pure cooldown rules for friend requests of one direction (sender → receiver).
 *
 * The n-th decline (withdrawal) in a row, each within the escalation window of the previous one,
 * blocks for `base * 2^(n-1)`, capped at the maximum cooldown.
 */
object FriendRequestCooldownCalculator {
    /** Doubling more often than this exceeds every sensible cap anyway and would overflow. */
    private const val MAX_DOUBLINGS = 30

    /** The cooldown after a [kind] event at [now], given the [previous] cooldown of that direction. */
    fun record(
        previous: FriendRequestCooldown?,
        kind: FriendRequestCooldownKind,
        now: OffsetDateTime,
        settings: FriendRequestCooldownSettings
    ): FriendRequestCooldown {
        val current = previous ?: FriendRequestCooldown.NONE
        val counter = nextCounter(current.counter(kind), now, settings.escalationWindow)
        val cooldown = cooldownFor(settings.baseCooldown(kind), counter.count, settings.maxCooldown)
        val expiresAt = latestOf(current.expiresAt, now.plus(cooldown.toJavaDuration()))

        return current.withCounter(kind, counter).copy(expiresAt = expiresAt)
    }

    /** Continues [previous] if it happened within [window] before [now], otherwise starts over. */
    fun nextCounter(previous: CooldownCounter, now: OffsetDateTime, window: Duration): CooldownCounter {
        val count = if (isWithinWindow(previous.lastAt, now, window)) previous.count + 1 else 1
        return CooldownCounter(count = count, lastAt = now)
    }

    /** The cooldown after the [count]-th event in a row. */
    fun cooldownFor(base: Duration, count: Int, max: Duration): Duration {
        val doublings = (count - 1).coerceIn(0, MAX_DOUBLINGS)
        return minOf(base * (1 shl doublings), max)
    }

    private fun isWithinWindow(lastAt: OffsetDateTime?, now: OffsetDateTime, window: Duration) =
        lastAt != null && !lastAt.plus(window.toJavaDuration()).isBefore(now)

    private fun latestOf(a: OffsetDateTime, b: OffsetDateTime) = if (a.isAfter(b)) a else b
}
