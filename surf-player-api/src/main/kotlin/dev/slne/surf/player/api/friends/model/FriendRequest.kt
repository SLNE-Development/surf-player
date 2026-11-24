package dev.slne.surf.social.api.friends.model

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.social.api.friends.player.FriendsPlayer
import kotlinx.serialization.Serializable

@Serializable
data class FriendRequest(
    val senderUuid: SerializableUUID,
    val targetUuid: SerializableUUID,
    val createdAt: SerializableOffsetDateTime
) {
    val sender get() = FriendsPlayer[senderUuid]
    val target get() = FriendsPlayer[targetUuid]
}