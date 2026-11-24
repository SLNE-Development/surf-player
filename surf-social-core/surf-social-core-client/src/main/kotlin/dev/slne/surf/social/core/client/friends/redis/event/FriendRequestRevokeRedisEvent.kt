package dev.slne.surf.social.core.client.friends.redis.event

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
data class FriendRequestRevokeRedisEvent(
    val executorUuid: SerializableUUID,
    val targetUuid: SerializableUUID,
    val notifyTarget: Boolean
) : RedisEvent()
