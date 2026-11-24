package dev.slne.surf.social.paper.chat.service

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.paper.nms.NmsUseWithCaution
import dev.slne.surf.api.paper.nms.bridges.SurfPaperNmsPlayerBridge
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.api.chat.message.MessageType
import dev.slne.surf.social.core.client.chat.hook.SettingsHook
import dev.slne.surf.social.core.client.chat.message.format.formatIncomingPm
import dev.slne.surf.social.core.client.chat.message.format.formatOutgoingPm
import dev.slne.surf.social.core.client.chat.processor.runPostProcessors
import dev.slne.surf.social.core.client.chat.processor.runPreProcessors
import dev.slne.surf.social.core.client.chat.redis.rpc.SendDirectMessageHandledRedisResponse
import dev.slne.surf.social.core.client.chat.redis.rpc.SendDirectMessageRedisRequest
import dev.slne.surf.social.core.client.chat.redis.rpc.SignedChatMessage
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.client.chat.service.ReplyCache
import dev.slne.surf.social.paper.chat.util.schedulePingSound
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.chat.SignedMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.OffsetDateTime
import java.util.*

/**
 * Sends signed direct messages (`/msg`, `/reply`) to players on this or another server.
 */
@OptIn(NmsUseWithCaution::class)
object DirectMessageSender {
    suspend fun sendMessage(sender: Player, message: SignedMessage, targetUuid: UUID) {
        val preProcessed = runPreProcessors(
            MessageContext(createMessageData(sender, message, targetUuid), false, mutableObjectSetOf())
        )
        val messageData = preProcessed.messageData

        if (preProcessed.isCancelled) {
            sender.sendText {
                appendErrorPrefix()
                error("Deine Nachricht konnte nicht zugestellt werden.")
            }
        } else {
            deliver(sender, message, messageData, targetUuid)
        }

        runPostProcessors(MessageContext(messageData, preProcessed.isCancelled, mutableObjectSetOf()))

        if (!preProcessed.isCancelled) {
            rememberReplyTargets(sender.uniqueId, targetUuid)
        }
    }

    /**
     * Shows a direct message forwarded from another server to its receiver on this server.
     *
     * @return whether the receiver is online here, i.e. this server handled the message
     */
    suspend fun receiveRemoteMessage(data: MessageData, signed: SignedChatMessage): Boolean {
        val target = data.receiver?.let { Bukkit.getPlayer(it) } ?: return false

        if (!acceptsDirectMessages(target)) {
            return true
        }

        pingIfEnabled(target)
        SignedMessageSender.showRemoteSignedMessage(target, data.sender, data.senderUser().username, signed)

        return true
    }

    private fun createMessageData(sender: Player, message: SignedMessage, targetUuid: UUID) = MessageData(
        message.unsignedContent() ?: text(message.message()),
        UUID.randomUUID(),
        sender.uniqueId,
        targetUuid,
        OffsetDateTime.now(),
        SurfCoreApi.getCurrentServerName(),
        null,
        MessageType.DIRECT
    )

    private suspend fun deliver(
        sender: Player,
        message: SignedMessage,
        messageData: MessageData,
        targetUuid: UUID
    ) {
        SurfPaperNmsPlayerBridge.sendSignedMessageWithChangedContent(
            sender,
            message,
            SurfPaperNmsPlayerBridge.getPaperRawChatType().bind(sender.name()),
            formatOutgoingPm(messageData)
        )

        val target = Bukkit.getPlayer(targetUuid)
        if (target != null) {
            sendOnSameServer(sender, target, messageData, message)
        } else {
            sendToOtherServer(sender, messageData, message)
        }
    }

    private suspend fun sendOnSameServer(
        sender: Player,
        target: Player,
        messageData: MessageData,
        signedMessage: SignedMessage,
    ) {
        if (!acceptsDirectMessages(target)) {
            return
        }

        pingIfEnabled(target)
        SignedMessageSender.sendLocalSignedMessage(sender, target, formatIncomingPm(messageData), signedMessage)
    }

    private suspend fun sendToOtherServer(
        sender: Player,
        messageData: MessageData,
        signedMessage: SignedMessage,
    ) {
        SignedMessageSender.forwardToRemoteServer(sender, signedMessage, formatIncomingPm(messageData)) { wireMessage ->
            SocialClient.redisApi.sendRequest<SendDirectMessageHandledRedisResponse>(
                SendDirectMessageRedisRequest(messageData, wireMessage),
            )
        }
    }

    private fun acceptsDirectMessages(target: Player) =
        SettingsHook.hasDirectMessagesEnabled(target.uniqueId)

    private fun pingIfEnabled(target: Player) {
        if (SettingsHook.hasChatPingsEnabled(target.uniqueId)) {
            target.schedulePingSound()
        }
    }

    private suspend fun rememberReplyTargets(sender: UUID, target: UUID) = coroutineScope {
        launch { ReplyCache.setLastTarget(sender, target) }
        launch { ReplyCache.setLastTarget(target, sender) }
    }
}
