package dev.slne.surf.player.core.client.chat.redis.event

import dev.slne.surf.api.core.serializer.adventure.component.SerializableComponent
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 * Shows the already formatted [message] to every player on every server who has [permission]:
 * team chat messages and AI moderation notifications.
 */
@Serializable
data class TeamBroadcastRedisEvent(
    val message: SerializableComponent,
    val permission: String
) : RedisEvent()
