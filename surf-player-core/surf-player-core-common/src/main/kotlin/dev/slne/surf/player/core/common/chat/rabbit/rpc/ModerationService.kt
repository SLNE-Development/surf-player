package dev.slne.surf.player.core.common.chat.rabbit.rpc

import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationResult
import dev.slne.surf.rabbitmq.api.rpc.RpcService

@RpcService
interface ModerationService {
    suspend fun logModeration(
        messageData: MessageData,
        classification: ModerationClassificationResult,
    )
}