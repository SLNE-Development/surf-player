package dev.slne.surf.social.core.client.chat.redis.event

import dev.slne.surf.social.api.chat.serializer.SerializableSignature
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
data class DeleteRemoteMessageRedisEvent(
    val messageSignature: SerializableSignature
) : RedisEvent()
