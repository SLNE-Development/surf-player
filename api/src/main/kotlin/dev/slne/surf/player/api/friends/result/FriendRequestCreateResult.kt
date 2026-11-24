package dev.slne.surf.player.api.friends.result

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.player.api.friends.model.FriendRequest
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed class FriendRequestCreateResult {
    @Serializable
    data class Success(
        val friendRequest: FriendRequest
    ) : FriendRequestCreateResult()

    @Serializable
    data class AlreadySentFriendRequest(
        val targetUuid: SerializableUUID
    ) : FriendRequestCreateResult()

    @Serializable
    data class AlreadyFriends(
        val targetUuid: SerializableUUID
    ) : FriendRequestCreateResult()

    @Serializable
    data class AlreadyReceivedFriendRequest(
        val targetUuid: SerializableUUID
    ) : FriendRequestCreateResult()

    /**
     * The sender may not send the target another request for [remaining], because the target
     * declined or the sender withdrew an earlier request.
     */
    @Serializable
    data class OnCooldown(
        val targetUuid: SerializableUUID,
        val remaining: Duration
    ) : FriendRequestCreateResult()

    /**
     * One of both players ignores the other. Deliberately carries no detail, so the sender cannot
     * tell this apart from other reasons the request could not be delivered.
     */
    @Serializable
    data class Blocked(
        val targetUuid: SerializableUUID
    ) : FriendRequestCreateResult()
}
