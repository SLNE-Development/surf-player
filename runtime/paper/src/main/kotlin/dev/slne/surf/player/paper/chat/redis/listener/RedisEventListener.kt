package dev.slne.surf.player.paper.chat.redis.listener

import dev.slne.surf.player.core.client.chat.redis.rpc.SendDirectMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendDirectMessageRedisRequest
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageRedisRequest
import dev.slne.surf.player.paper.chat.service.DirectMessageSender
import dev.slne.surf.player.paper.chat.service.SignedMessageSender
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext
import kotlinx.coroutines.launch
import org.bukkit.Bukkit

/**
 * Handles the chat Redis requests that need Paper types. The platform independent chat events are
 * handled by [dev.slne.surf.player.core.client.chat.redis.ChatRedisListener].
 */
object RedisEventListener {
    @HandleRedisRequest
    fun handleSendDirectMessageRedisRequest(context: RequestContext<SendDirectMessageRedisRequest>) {
        val (messageData, message) = context.request
        val receiver = messageData.receiver ?: return
        if (Bukkit.getPlayer(receiver) == null) return

        context.launch {
            val handled = DirectMessageSender.receiveRemoteMessage(messageData, message)
            if (handled) {
                context.respond(SendDirectMessageHandledRedisResponse()).await()
            }
        }
    }

    @HandleRedisRequest
    fun handleSendSignedMessageRedisRequest(context: RequestContext<SendSignedMessageRedisRequest>) = context.launch {
        val handled = SignedMessageSender.handleRemoteSignedMessage(
            context.request.sender,
            context.request.senderName,
            context.request.target,
            context.request.message
        )

        if (handled) {
            context.respond(SendSignedMessageHandledRedisResponse()).await()
        }
    }
}
