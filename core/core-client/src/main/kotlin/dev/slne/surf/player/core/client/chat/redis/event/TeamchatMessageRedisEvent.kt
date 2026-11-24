package dev.slne.surf.player.core.client.chat.redis.event

import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
data class TeamchatMessageRedisEvent(
    val messageData: MessageData
) : RedisEvent()