package dev.slne.surf.player.core.client.chat.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.chat.SurfChatApi
import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import dev.slne.surf.player.api.chat.entry.IgnoreListEntry
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.redirector.MessageRedirector
import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.chat.message.MessageRedirectorRegistry
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.processor.ChatProcessorRegistry
import dev.slne.surf.player.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.player.core.client.chat.redis.ModerationRedisService
import dev.slne.surf.player.core.client.chat.redis.event.DeleteRemoteMessageRedisEvent
import dev.slne.surf.player.core.client.chat.service.HistoryService
import dev.slne.surf.player.core.client.chat.service.IgnoreService
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfChatApi::class)
class SurfChatApiImpl : SurfChatApi, Services.Fallback {
    override suspend fun logMessage(data: MessageData) {
        HistoryService.logMessage(data)
        ModerationRedisService.cache(data)
    }

    override fun registerChatProcessor(processor: PreChatProcessor) {
        ChatProcessorRegistry.register(processor)
    }

    override fun registerChatProcessor(processor: PostChatProcessor) {
        ChatProcessorRegistry.register(processor)
    }

    override fun registerMessageRedirector(redirector: MessageRedirector) {
        MessageRedirectorRegistry.register(redirector)
    }

    override fun cachedIgnoreList(uuid: UUID): List<IgnoreListEntry> =
        IgnoreService.getCachedIgnoreList(uuid)

    override suspend fun ignoreList(uuid: UUID): List<IgnoreListEntry> =
        IgnoreService.loadIgnoreList(uuid)

    override fun deleteMessage(signature: SignedMessage.Signature) =
        ChatPlatform.deleteMessage(signature)

    override suspend fun deleteRemoteMessage(deleter: UUID?, messageData: MessageData) {
        messageData.signature?.let {
            SurfPlayerClient.redisApi.publishEvent(DeleteRemoteMessageRedisEvent(it))
        }

        HistoryService.markDeleted(messageData.messageUuid, deleter, null)
    }

    override suspend fun lookupHistory(filter: HistoryFilter): ObjectList<HistoryEntry> =
        HistoryService.findHistoryEntry(filter)

    override suspend fun sendSignedMessage(
        senderUuid: UUID,
        targetUuid: UUID,
        signedMessage: SignedMessage,
        contentComponent: Component
    ) = ChatPlatform.sendSignedMessage(senderUuid, targetUuid, signedMessage, contentComponent)

    override suspend fun passAutoMod(messageData: MessageData) {
        AiModerationPostChatProcessor.processMessage(messageData)
    }
}
