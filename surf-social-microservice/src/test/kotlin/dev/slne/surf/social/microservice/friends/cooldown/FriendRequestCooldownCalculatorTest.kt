package dev.slne.surf.social.microservice.friends.cooldown

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class FriendRequestCooldownCalculatorTest {
    private val settings = FriendRequestCooldownSettings.DEFAULT
    private val start = OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)

    @Test
    fun `first decline blocks for the base decline cooldown`() {
        val cooldown = record(null, FriendRequestCooldownKind.DECLINE, start)

        assertEquals(24.hours, cooldown.remainingAt(start))
    }

    @Test
    fun `first withdrawal blocks for the base withdraw cooldown`() {
        val cooldown = record(null, FriendRequestCooldownKind.WITHDRAW, start)

        assertEquals(30.minutes, cooldown.remainingAt(start))
    }

    @Test
    fun `each further decline within the window doubles the cooldown`() {
        val first = record(null, FriendRequestCooldownKind.DECLINE, start)
        val secondAt = start + 2.days
        val second = record(first, FriendRequestCooldownKind.DECLINE, secondAt)
        val thirdAt = secondAt + 3.days
        val third = record(second, FriendRequestCooldownKind.DECLINE, thirdAt)

        assertEquals(48.hours, second.remainingAt(secondAt))
        assertEquals(96.hours, third.remainingAt(thirdAt))
    }

    @Test
    fun `each further withdrawal within the window doubles the cooldown`() {
        val first = record(null, FriendRequestCooldownKind.WITHDRAW, start)
        val secondAt = start + 1.hours
        val second = record(first, FriendRequestCooldownKind.WITHDRAW, secondAt)

        assertEquals(60.minutes, second.remainingAt(secondAt))
    }

    @Test
    fun `escalation is capped at the maximum cooldown`() {
        var cooldown: FriendRequestCooldown? = null
        var now = start

        repeat(6) {
            now += 8.days
            cooldown = record(cooldown, FriendRequestCooldownKind.DECLINE, now)
        }

        assertEquals(7.days, cooldown!!.remainingAt(now))
    }

    @Test
    fun `cooldown starts over after a pause longer than the window`() {
        val first = record(null, FriendRequestCooldownKind.DECLINE, start)
        val second = record(first, FriendRequestCooldownKind.DECLINE, start + 2.days)
        val thirdAt = start + 2.days + 31.days
        val third = record(second, FriendRequestCooldownKind.DECLINE, thirdAt)

        assertEquals(1, third.declines.count)
        assertEquals(24.hours, third.remainingAt(thirdAt))
    }

    @Test
    fun `an event exactly at the end of the window still escalates`() {
        val first = record(null, FriendRequestCooldownKind.DECLINE, start)
        val secondAt = start + 30.days
        val second = record(first, FriendRequestCooldownKind.DECLINE, secondAt)

        assertEquals(48.hours, second.remainingAt(secondAt))
    }

    @Test
    fun `declines and withdrawals escalate independently`() {
        val decline = record(null, FriendRequestCooldownKind.DECLINE, start)
        val withdrawAt = start + 2.days
        val withdrawal = record(decline, FriendRequestCooldownKind.WITHDRAW, withdrawAt)

        assertEquals(1, withdrawal.declines.count)
        assertEquals(1, withdrawal.withdrawals.count)
        assertEquals(30.minutes, withdrawal.remainingAt(withdrawAt))
    }

    @Test
    fun `a shorter new cooldown never shortens a running one`() {
        val decline = record(null, FriendRequestCooldownKind.DECLINE, start)
        val withdrawal = record(decline, FriendRequestCooldownKind.WITHDRAW, start + 1.hours)

        assertEquals(decline.expiresAt, withdrawal.expiresAt)
    }

    @Test
    fun `cooldown is over once it expired`() {
        val cooldown = record(null, FriendRequestCooldownKind.WITHDRAW, start)

        assertNull(cooldown.remainingAt(start + 30.minutes))
    }

    @Test
    fun `cooldown for many events stays at the maximum`() {
        assertEquals(7.days, FriendRequestCooldownCalculator.cooldownFor(24.hours, 1_000, 7.days))
    }

    @Test
    fun `cooldown for the first event is the base`() {
        assertEquals(30.minutes, FriendRequestCooldownCalculator.cooldownFor(30.minutes, 1, 7.days))
    }

    private fun record(
        previous: FriendRequestCooldown?,
        kind: FriendRequestCooldownKind,
        now: OffsetDateTime
    ) = FriendRequestCooldownCalculator.record(previous, kind, now, settings)

    private operator fun OffsetDateTime.plus(duration: Duration): OffsetDateTime = plus(duration.toJavaDuration())
}
