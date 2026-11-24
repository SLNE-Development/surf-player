package dev.slne.surf.player.core.client.friends.redis.event

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
data class FriendRemoveRedisEvent(
    val executorUuid: SerializableUUID,
    val targetUuid: SerializableUUID
) : RedisEvent()
