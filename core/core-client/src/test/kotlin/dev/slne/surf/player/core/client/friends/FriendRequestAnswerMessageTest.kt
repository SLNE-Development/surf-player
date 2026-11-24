package dev.slne.surf.player.core.client.friends

import dev.slne.surf.player.api.friends.result.FriendRequestStateResult
import dev.slne.surf.player.core.client.friends.command.NO_RECEIVED_FRIEND_REQUEST_MESSAGE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.*

class FriendRequestAnswerMessageTest {
    @Test
    fun `a successful answer is confirmed by the friends event, not here`() {
        assertNull(friendRequestAnswerFailureMessage(FriendRequestStateResult.Success))
    }

    @Test
    fun `answering a request that is gone tells the player, by click and by command alike`() {
        assertEquals(
            NO_RECEIVED_FRIEND_REQUEST_MESSAGE,
            friendRequestAnswerFailureMessage(FriendRequestStateResult.NoFriendRequest(UUID.randomUUID())),
        )
    }
}
