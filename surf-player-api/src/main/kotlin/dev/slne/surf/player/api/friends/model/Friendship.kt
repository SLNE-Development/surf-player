package dev.slne.surf.player.api.friends.model

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.player.api.friends.player.FriendsPlayer
import kotlinx.serialization.Serializable

@Serializable
data class Friendship(
    val playerUuid: SerializableUUID,
    val friendUuid: SerializableUUID,
    val createdAt: SerializableOffsetDateTime
) {
    val player get() = FriendsPlayer[playerUuid]
    val friend get() = FriendsPlayer[friendUuid]
}