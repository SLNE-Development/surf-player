package dev.slne.surf.social.core.common.chat.rabbit.rpc

import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.core.common.chat.aimoderation.ModerationClassificationResult
import dev.slne.surf.rabbitmq.api.rpc.RpcService

@RpcService
interface ModerationService {
    suspend fun logModeration(
        messageData: MessageData,
        classification: ModerationClassificationResult,
    )
}