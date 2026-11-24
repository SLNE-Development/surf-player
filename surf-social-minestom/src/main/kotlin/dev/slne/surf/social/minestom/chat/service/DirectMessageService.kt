package dev.slne.surf.social.minestom.chat.service

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.minestom.chat.RemoteChatSender
import dev.slne.surf.api.minestom.chat.sendRemoteSignedMessage
import dev.slne.surf.api.minestom.chat.sendSignedMessage
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.api.chat.message.MessageType
import dev.slne.surf.social.core.client.chat.hook.SettingsHook
import dev.slne.surf.social.core.client.chat.message.format.formatIncomingPm
import dev.slne.surf.social.core.client.chat.message.format.formatOutgoingPm
import dev.slne.surf.social.core.client.chat.platform.ChatPlatform
import dev.slne.surf.social.core.client.chat.processor.runPostProcessors
import dev.slne.surf.social.core.client.chat.processor.runPreProcessors
import dev.slne.surf.social.core.client.chat.redis.rpc.SendDirectMessageHandledRedisResponse
import dev.slne.surf.social.core.client.chat.redis.rpc.SendDirectMessageRedisRequest
import dev.slne.surf.social.core.client.chat.redis.rpc.SignedChatMessage
import dev.slne.surf.social.core.client.SocialClient.redisApi
import dev.slne.surf.social.core.client.chat.service.ReplyCache
import dev.slne.surf.social.minestom.chat.player.boundDisplayName
import dev.slne.surf.social.minestom.chat.redis.rpc.chatSession
import dev.slne.surf.social.minestom.chat.redis.rpc.toRemote
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.redis.request.RequestTimeoutException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.chat.SignedMessage
import net.minestom.server.entity.Player
import java.time.OffsetDateTime
import java.util.*

/**
 * Delivers direct messages between players, no matter which server of the network they are on.
 */
object DirectMessageService {
    private val log = logger()

    suspend fun sendMessage(sender: Player, message: SignedMessage, targetUuid: UUID) {
        var messageData = MessageData(
            message.unsignedContent() ?: text(message.message()),
            UUID.randomUUID(),
            sender.uuid,
            targetUuid,
            OffsetDateTime.now(),
            SurfCoreApi.getCurrentServerName(),
            null,
            MessageType.DIRECT
        )

        val result = runPreProcessors(MessageContext(messageData, false, mutableObjectSetOf()))
        messageData = result.messageData

        if (result.isCancelled) {
            sender.sendText {
                appendErrorPrefix()
                error("Deine Nachricht konnte nicht zugestellt werden.")
            }
        } else {
            sender.sendSignedMessage(
                message,
                sender.boundDisplayName(),
                formatOutgoingPm(messageData)
            )

            val target = ConnectionManager.getOnlinePlayerByUuid(targetUuid)

            if (target != null) {
                sendPmOnSameServer(sender, target, messageData, message)
            } else {
                sendPmOnDifferentServer(sender, messageData, message)
            }
        }

        runPostProcessors(
            MessageContext(
                messageData,
                result.isCancelled,
                mutableObjectSetOf()
            )
        )

        if (!result.isCancelled) {
            coroutineScope {
                launch { ReplyCache.setLastTarget(sender.uuid, targetUuid) }
                launch { ReplyCache.setLastTarget(targetUuid, sender.uuid) }
            }
        }
    }

    private suspend fun sendPmOnSameServer(
        sender: Player,
        target: Player,
        messageData: MessageData,
        message: SignedMessage,
    ) {
        if (!SettingsHook.hasDirectMessagesEnabled(target.uuid)) return

        target.sendSignedMessage(
            message,
            sender.boundDisplayName(),
            formatIncomingPm(messageData)
        )

        if (SettingsHook.hasChatPingsEnabled(target.uuid)) {
            ChatPlatform.playPingSound(target.uuid)
        }
    }

    private suspend fun sendPmOnDifferentServer(
        sender: Player,
        messageData: MessageData,
        message: SignedMessage,
    ) {
        val captured = SignedMessageService.captureForRemoteDelivery(
            sender,
            message,
            formatIncomingPm(messageData)
        )

        try {
            redisApi.sendRequest<SendDirectMessageHandledRedisResponse>(
                SendDirectMessageRedisRequest(messageData, captured),
            )
        } catch (_: RequestTimeoutException) {
            log.atFine().log("No server delivered the direct message ${messageData.messageUuid}")
        }
    }

    /**
     * Shows a direct message that was sent on another server to its receiver here.
     *
     * @return whether the receiver is on this server, no matter if they wanted to see the message
     */
    suspend fun handleRemotePm(messageData: MessageData, message: SignedChatMessage): Boolean {
        val target = messageData.receiver
            ?.let { ConnectionManager.getOnlinePlayerByUuid(it) } ?: return false

        if (!SettingsHook.hasDirectMessagesEnabled(target.uuid)) return true

        val senderUser = messageData.senderUser()

        target.sendRemoteSignedMessage(
            RemoteChatSender(messageData.sender, senderUser.username, message.chatSession()),
            message.toRemote(),
            text(senderUser.username)
        )

        if (SettingsHook.hasChatPingsEnabled(target.uuid)) {
            ChatPlatform.playPingSound(target.uuid)
        }

        return true
    }
}
