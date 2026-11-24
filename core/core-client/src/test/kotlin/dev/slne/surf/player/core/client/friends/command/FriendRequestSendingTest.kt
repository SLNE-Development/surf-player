package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.core.client.friends.friendRequestsDisabledMessage
import dev.slne.surf.player.core.client.util.undeliverableMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.*

class FriendRequestSendingTest {
    private val target = SurfPlayer(
        uuid = UUID.randomUUID(),
        lastKnownName = "Keviro",
        firstSeen = null,
        lastSeen = null,
        transferred = false,
    )

    @Test
    fun `a request blocked by an ignore reads like disabled requests`() {
        val blocked = failureMessage(FriendRequestCreateResult.Blocked(target.uuid), target)

        assertEquals(friendRequestsDisabledMessage(target), blocked)
        assertEquals(failureMessage(FriendRequestCreateResult.RequestsDisabled(target.uuid), target), blocked)
    }

    @Test
    fun `a blocked request does not use the undeliverable message of direct messages`() {
        assertNotEquals(undeliverableMessage(), failureMessage(FriendRequestCreateResult.Blocked(target.uuid), target))
    }
}
