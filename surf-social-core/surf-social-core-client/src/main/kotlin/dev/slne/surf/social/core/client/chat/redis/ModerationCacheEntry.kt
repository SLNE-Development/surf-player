package dev.slne.surf.social.core.client.chat.redis

import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.core.common.chat.aimoderation.ModerationClassificationResult
import kotlinx.serialization.Serializable

/**
 * A moderated message as it is kept in the moderation cache on Redis.
 */
@Serializable
internal data class ModerationCacheEntry(
    val messageData: MessageData,
    val classificationResult: ModerationClassificationResult
)
