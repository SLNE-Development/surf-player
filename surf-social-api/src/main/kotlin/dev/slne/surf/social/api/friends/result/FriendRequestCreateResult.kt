package dev.slne.surf.social.api.friends.result

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.social.api.friends.model.FriendRequest
import kotlinx.serialization.Serializable

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
}