package dev.slne.surf.player.core.client.friends.redis.event

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 * Tells every server that [executorUuid] did [action] towards [targetUuid], so the servers hosting
 * one of both players can notify them. The synchronized friends state is already updated when this
 * is published.
 */
@Serializable
data class FriendsRedisEvent(
    val action: FriendsAction,
    val executorUuid: SerializableUUID,
    val targetUuid: SerializableUUID
) : RedisEvent()
