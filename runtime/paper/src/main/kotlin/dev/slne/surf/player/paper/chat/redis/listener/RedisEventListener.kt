package dev.slne.surf.player.paper.chat.redis.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.client.chat.message.format.formatTeamchat
import dev.slne.surf.player.core.client.chat.redis.event.DeleteRemoteMessageRedisEvent
import dev.slne.surf.player.core.client.chat.redis.event.TeamMessageRedisEvent
import dev.slne.surf.player.core.client.chat.redis.event.TeamchatMessageRedisEvent
import dev.slne.surf.player.core.client.chat.redis.rpc.SendDirectMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendDirectMessageRedisRequest
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageRedisRequest
import dev.slne.surf.player.paper.chat.permission.PermissionRegistry
import dev.slne.surf.player.paper.chat.service.DirectMessageSender
import dev.slne.surf.player.paper.chat.service.SignedMessageSender
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext
import kotlinx.coroutines.launch
import org.bukkit.Bukkit

object RedisEventListener {
    @OnRedisEvent
    fun onTeamchatMessage(event: TeamchatMessageRedisEvent) {
        plugin.launch {
            val message = formatTeamchat(event.messageData)
            Bukkit.broadcast(message, PermissionRegistry.COMMAND_TEAMCHAT)
        }
    }

    @OnRedisEvent
    fun onTeamMessage(event: TeamMessageRedisEvent) {
        val message = event.message
        Bukkit.broadcast(message, PermissionRegistry.PREFIX_TEAM)
    }

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

    @OnRedisEvent
    fun handleDeleteRemoteChatMessage(event: DeleteRemoteMessageRedisEvent) {
        Bukkit.getServer().deleteMessage(event.messageSignature)
    }
}