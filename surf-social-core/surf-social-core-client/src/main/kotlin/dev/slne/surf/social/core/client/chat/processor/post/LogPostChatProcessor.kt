package dev.slne.surf.social.core.client.chat.processor.post

import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.processor.PostChatProcessor
import dev.slne.surf.social.core.client.chat.redis.ModerationRedisService
import dev.slne.surf.social.core.common.chat.service.HistoryService

object LogPostChatProcessor : PostChatProcessor {
    override suspend fun process(messageContext: MessageContext) {
        HistoryService.logMessage(messageContext.messageData)
        ModerationRedisService.cache(messageContext.messageData)
    }
}
