package dev.slne.surf.player.core.common.chat.rpc

import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationResult
import dev.slne.surf.rabbitmq.api.rpc.RpcService

/** The log of messages the AI moderation flagged, stored by the microservice. */
@RpcService
interface ChatModerationRpcService {
    suspend fun createModerationLog(
        messageData: MessageData,
        classification: ModerationClassificationResult,
    )
}
