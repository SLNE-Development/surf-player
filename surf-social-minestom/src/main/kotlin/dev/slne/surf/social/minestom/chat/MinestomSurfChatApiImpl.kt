package dev.slne.surf.social.minestom.chat

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.minestom.chat.sendSignedMessage
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.social.api.chat.SurfChatApi
import dev.slne.surf.social.api.chat.entry.HistoryEntry
import dev.slne.surf.social.api.chat.entry.HistoryFilter
import dev.slne.surf.social.api.chat.entry.IgnoreListEntry
import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.api.chat.processor.PostChatProcessor
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.api.chat.processor.chatProcessorRegistry
import dev.slne.surf.social.core.client.chat.platform.ChatPlatform
import dev.slne.surf.social.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.social.core.client.chat.redis.ModerationRedisService
import dev.slne.surf.social.core.client.chat.redis.event.DeleteRemoteMessageRedisEvent
import dev.slne.surf.social.core.client.chat.redisApi
import dev.slne.surf.social.core.common.chat.service.HistoryService
import dev.slne.surf.social.core.common.chat.service.IgnoreService
import dev.slne.surf.social.minestom.chat.player.boundDisplayName
import dev.slne.surf.social.minestom.chat.service.SignedMessageService
import dev.slne.surf.core.api.common.SurfCoreApi
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Services
import java.util.UUID

@AutoService(SurfChatApi::class)
class MinestomSurfChatApiImpl : SurfChatApi, Services.Fallback {
    private val log = logger()

    override suspend fun logMessage(data: MessageData) {
        HistoryService.logMessage(data)
        ModerationRedisService.cache(data)
    }

    override fun deleteMessage(signature: SignedMessage.Signature) =
        ChatPlatform.deleteMessage(signature)

    override suspend fun deleteRemoteMessage(deleter: UUID?, messageData: MessageData) {
        messageData.signature?.let {
            redisApi.publishEvent(DeleteRemoteMessageRedisEvent(it))
        }

        HistoryService.markDeleted(messageData.messageUuid, deleter, null)
    }

    override fun registerChatProcessor(processor: PreChatProcessor) {
        chatProcessorRegistry.register(processor)
    }

    override fun registerChatProcessor(processor: PostChatProcessor) {
        chatProcessorRegistry.register(processor)
    }

    override fun getCachedIgnoreList(uuid: UUID): List<IgnoreListEntry> =
        IgnoreService.getCachedIgnoreList(uuid)

    override suspend fun getIgnoreList(uuid: UUID): List<IgnoreListEntry> =
        IgnoreService.loadIgnoreList(uuid)

    override suspend fun lookupHistory(filter: HistoryFilter): ObjectList<HistoryEntry> =
        HistoryService.findHistoryEntry(filter)

    override suspend fun sendSignedMessage(
        signedMessage: SignedMessage,
        senderUuid: UUID,
        targetUuid: UUID,
        contentComponent: Component
    ) {
        val sender = ConnectionManager.getOnlinePlayerByUuid(senderUuid)

        if (sender == null) {
            log.atWarning()
                .log("Tried to send signed message from offline player $senderUuid to $targetUuid!")
            return
        }

        val target = ConnectionManager.getOnlinePlayerByUuid(targetUuid)

        if (target != null) {
            target.sendSignedMessage(signedMessage, sender.boundDisplayName(), contentComponent)
            return
        }

        if (SurfCoreApi.getPlayer(targetUuid) == null) {
            log.atWarning()
                .log("Tried to send signed message from $senderUuid to offline player $targetUuid!")
            return
        }

        SignedMessageService.sendRemoteSignedMessage(
            sender,
            targetUuid,
            contentComponent,
            signedMessage
        )
    }

    override suspend fun passAutoMod(messageData: MessageData) {
        AiModerationPostChatProcessor.processMessage(messageData)
    }
}
