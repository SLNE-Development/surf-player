package dev.slne.surf.player.minestom.chat.redis.listener

import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.player.core.client.chat.redis.rpc.SendDirectMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendDirectMessageRedisRequest
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageRedisRequest
import dev.slne.surf.player.minestom.chat.service.DirectMessageService
import dev.slne.surf.player.minestom.chat.service.SignedMessageService
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext
import kotlinx.coroutines.launch

/**
 * Handles the chat Redis requests that need Minestom types. The platform independent chat events
 * are handled by [dev.slne.surf.player.core.client.chat.redis.ChatRedisListener].
 */
object MinestomRedisEventListener {

    @HandleRedisRequest
    fun handleSendDirectMessageRedisRequest(context: RequestContext<SendDirectMessageRedisRequest>) {
        val (messageData, message) = context.request
        val receiver = messageData.receiver ?: return
        if (ConnectionManager.getOnlinePlayerByUuid(receiver) == null) return

        context.launch {
            val handled = DirectMessageService.handleRemotePm(messageData, message)
            if (handled) {
                context.respond(SendDirectMessageHandledRedisResponse()).await()
            }
        }
    }

    @HandleRedisRequest
    fun handleSendSignedMessageRedisRequest(context: RequestContext<SendSignedMessageRedisRequest>) {
        val (sender, senderName, target, message) = context.request
        if (ConnectionManager.getOnlinePlayerByUuid(target) == null) return

        context.launch {
            val handled =
                SignedMessageService.handleRemoteSignedMessage(sender, senderName, target, message)

            if (handled) {
                context.respond(SendSignedMessageHandledRedisResponse()).await()
            }
        }
    }
}
