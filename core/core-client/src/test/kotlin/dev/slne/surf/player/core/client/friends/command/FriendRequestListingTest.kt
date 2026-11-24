package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.player.api.friends.model.FriendRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class FriendRequestListingTest {
    private val target = UUID.randomUUID()
    private val known = UUID.randomUUID()
    private val unknown = UUID.randomUUID()
    private val now = OffsetDateTime.now()

    @Test
    fun `a sender whose name cannot be resolved is listed by uuid instead of left out`() = runBlocking {
        val rows = friendRequestRows(
            listOf(FriendRequest(unknown, target, now), FriendRequest(known, target, now.minusMinutes(1))),
        ) { uuid -> if (uuid == known) "DevPlayer1" else null }

        assertEquals(listOf(unknown.toString(), "DevPlayer1"), rows.map { it.requesterName })
    }

    @Test
    fun `requests are listed newest first`() = runBlocking {
        val older = UUID.randomUUID()
        val rows = friendRequestRows(
            listOf(FriendRequest(older, target, now.minusHours(1)), FriendRequest(known, target, now)),
        ) { uuid -> uuid.toString().take(4) }

        assertEquals(listOf(now, now.minusHours(1)), rows.map { it.createdAt })
    }
}
