package dev.slne.surf.player.core.client.chat.redis

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationResult
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.redis.codec.JsonKotlinCodec
import java.util.concurrent.TimeUnit

object ModerationRedisService {
    private val log = logger()

    private val moderationCache by lazy {
        redisApi.redisson.getSetCache<ModerationCacheEntry>(
            "surf-chat:v3:moderations",
            JsonKotlinCodec.of<ModerationCacheEntry>()
        )
    }

    private val chatMessageCache by lazy {
        redisApi.redisson.getSetCache<MessageData>(
            "surf-chat:v3:messages",
            JsonKotlinCodec.of<MessageData>()
        )
    }

    fun cache(messageData: MessageData, classificationResult: ModerationClassificationResult) {
        moderationCache.addAsync(ModerationCacheEntry(messageData, classificationResult), 1, TimeUnit.HOURS)
            .exceptionally { throwable ->
                log.atWarning()
                    .withCause(throwable)
                    .log("Failed to cache moderation result for message ${messageData.messageUuid}")
                null
            }
    }

    fun cache(messageData: MessageData) {
        chatMessageCache.addAsync(messageData, 1, TimeUnit.HOURS).exceptionally { throwable ->
            log.atWarning()
                .withCause(throwable)
                .log("Failed to cache message ${messageData.messageUuid}")
            null
        }
    }
}