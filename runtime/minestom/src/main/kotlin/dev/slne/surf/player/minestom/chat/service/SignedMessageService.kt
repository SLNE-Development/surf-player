package dev.slne.surf.player.minestom.chat.service

import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.minestom.chat.RemoteChatSender
import dev.slne.surf.api.minestom.chat.captureSignedMessage
import dev.slne.surf.api.minestom.chat.sendRemoteSignedMessage
import dev.slne.surf.api.minestom.chat.signedChatSession
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.redis.request.RequestTimeoutException
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageRedisRequest
import dev.slne.surf.player.core.client.chat.redis.rpc.SignedChatMessage
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.minestom.chat.redis.rpc.chatSession
import dev.slne.surf.player.minestom.chat.redis.rpc.toRemote
import dev.slne.surf.player.minestom.chat.redis.rpc.toWire
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import java.util.*

/**
 * Delivers signed messages to players that are not on this server.
 */
object SignedMessageService {
    private val log = logger()

    /**
     * Asks the server [targetUuid] is on to show [signedMessage] with [component] as its content.
     */
    suspend fun sendRemoteSignedMessage(
        sender: Player,
        targetUuid: UUID,
        component: Component,
        signedMessage: SignedMessage,
    ) {
        val request = SendSignedMessageRedisRequest(
            sender.uuid,
            sender.username,
            targetUuid,
            captureForRemoteDelivery(sender, signedMessage, component)
        )

        try {
            redisApi.sendRequest<SendSignedMessageHandledRedisResponse>(request)
        } catch (_: RequestTimeoutException) {
            log.atFine().log("No server delivered the signed message from ${sender.uuid} to $targetUuid")
        }
    }

    /**
     * Captures [signedMessage] of [sender], shown with [component] as its content, so another
     * server of the network can show it as a signed message.
     */
    fun captureForRemoteDelivery(
        sender: Player,
        signedMessage: SignedMessage,
        component: Component
    ): SignedChatMessage {
        val captured = requireNotNull(sender.captureSignedMessage(signedMessage, component)) {
            "Failed to capture the signed message."
        }

        return captured.toWire(sender.signedChatSession)
    }

    /**
     * Shows a signed message that was sent on another server to its target here.
     *
     * @return whether the target is on this server
     */
    fun handleRemoteSignedMessage(
        sender: UUID,
        senderName: String,
        target: UUID,
        message: SignedChatMessage
    ): Boolean {
        val targetPlayer = ConnectionManager.getOnlinePlayerByUuid(target) ?: return false

        targetPlayer.sendRemoteSignedMessage(
            RemoteChatSender(sender, senderName, message.chatSession()),
            message.toRemote(),
            text(senderName)
        )

        return true
    }
}
