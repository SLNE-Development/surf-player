package dev.slne.surf.player.core.client.chat.redis.event

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 * The delivered private message [messageData] is shown to the [spies] reading the private
 * messages of its sender or receiver; every server shows it to the spies it hosts, so the
 * teleport button's click callback is registered where it is clicked.
 */
@Serializable
data class PrivateMessageSpyRedisEvent(
    val messageData: MessageData,
    val spies: List<SerializableUUID>,
) : RedisEvent()
