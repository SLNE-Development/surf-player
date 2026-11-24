package dev.slne.surf.player.core.client.chat.redis

import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationResult
import kotlinx.serialization.Serializable

/**
 * A moderated message as it is kept in the moderation cache on Redis.
 */
@Serializable
internal data class ModerationCacheEntry(
    val messageData: MessageData,
    val classificationResult: ModerationClassificationResult
)
