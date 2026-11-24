package dev.slne.surf.player.core.client.chat.redis.event

import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationAction
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 * The AI moderation flagged [messageData] with [action]. Every server builds the notice for its
 * team members itself, so the delete button's click callback is registered on the server the
 * clicking player is on; a callback built on the originating server would not work anywhere else.
 */
@Serializable
data class AiModerationNoticeRedisEvent(
    val messageData: MessageData,
    val action: ModerationClassificationAction
) : RedisEvent()
