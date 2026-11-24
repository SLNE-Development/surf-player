package dev.slne.surf.player.microservice.friends.rpc

import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.core.common.friends.rpc.FriendRequestPrivileges
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class FriendRequestGateTest {
    private val target = UUID.randomUUID()

    private fun refusal(
        privileges: FriendRequestPrivileges = FriendRequestPrivileges.NONE,
        senderIgnoresTarget: Boolean = false,
        targetIgnoresSender: Boolean = false,
        acceptsRequests: Boolean = true,
        cooldown: Duration? = null,
    ) = runBlocking {
        FriendRequestGate.refusal(
            target,
            privileges,
            senderIgnoresTarget = { senderIgnoresTarget },
            targetIgnoresSender = { targetIgnoresSender },
            targetAcceptsRequests = { acceptsRequests },
            remainingCooldown = { cooldown },
        )
    }

    @Test
    fun `a request without obstacles may be stored`() {
        assertNull(refusal())
    }

    @Test
    fun `either ignore blocks a normal sender`() {
        assertEquals(FriendRequestCreateResult.Blocked(target), refusal(senderIgnoresTarget = true))
        assertEquals(FriendRequestCreateResult.Blocked(target), refusal(targetIgnoresSender = true))
    }

    @Test
    fun `the target's ignore does not block a team member`() {
        val exempt = FriendRequestPrivileges(ignoreExempt = true)

        assertNull(refusal(exempt, targetIgnoresSender = true))
        assertEquals(FriendRequestCreateResult.Blocked(target), refusal(exempt, senderIgnoresTarget = true))
    }

    @Test
    fun `disabled requests are reported after the ignore check`() {
        assertEquals(FriendRequestCreateResult.RequestsDisabled(target), refusal(acceptsRequests = false))
        assertEquals(
            FriendRequestCreateResult.Blocked(target),
            refusal(targetIgnoresSender = true, acceptsRequests = false)
        )
    }

    @Test
    fun `a cooldown blocks a normal sender`() {
        assertEquals(FriendRequestCreateResult.OnCooldown(target, 2.hours), refusal(cooldown = 2.hours))
    }

    @Test
    fun `the cooldown bypass ignores cooldowns but not the other checks`() {
        val bypass = FriendRequestPrivileges(cooldownBypass = true)

        assertNull(refusal(bypass, cooldown = 2.hours))
        assertEquals(FriendRequestCreateResult.RequestsDisabled(target), refusal(bypass, acceptsRequests = false))
        assertEquals(FriendRequestCreateResult.Blocked(target), refusal(bypass, targetIgnoresSender = true))
    }

    @Test
    fun `the cooldown is not looked up for a bypassing sender`() {
        var lookedUp = false

        runBlocking {
            FriendRequestGate.refusal(
                target,
                FriendRequestPrivileges(cooldownBypass = true),
                senderIgnoresTarget = { false },
                targetIgnoresSender = { false },
                targetAcceptsRequests = { true },
                remainingCooldown = { lookedUp = true; 1.hours },
            )
        }

        assertFalse(lookedUp)
    }
}
