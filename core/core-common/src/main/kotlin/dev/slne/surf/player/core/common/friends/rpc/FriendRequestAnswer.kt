package dev.slne.surf.player.core.common.friends.rpc

import dev.slne.surf.player.api.friends.model.Friendship
import kotlinx.serialization.Serializable

/**
 * What the microservice did when a friend request was answered.
 */
@Serializable
sealed interface FriendRequestAnswer {
    /**
     * The friendship was created; [friendships] are both stored directions, with the timestamp the
     * microservice stored, so clients add exactly what the database holds.
     */
    @Serializable
    data class Accepted(val friendships: List<Friendship>) : FriendRequestAnswer

    /** The request was declined. */
    @Serializable
    data object Declined : FriendRequestAnswer
}
