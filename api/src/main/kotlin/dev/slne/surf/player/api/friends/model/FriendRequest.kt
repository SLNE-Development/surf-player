package dev.slne.surf.player.api.friends.model

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.player.api.SurfCommunityPlayer
import kotlinx.serialization.Serializable

@Serializable
data class FriendRequest(
    val senderUuid: SerializableUUID,
    val targetUuid: SerializableUUID,
    val createdAt: SerializableOffsetDateTime
) {
    val sender get() = SurfCommunityPlayer(senderUuid)
    val target get() = SurfCommunityPlayer(targetUuid)
}
