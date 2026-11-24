package dev.slne.surf.social.paper.chat

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.chat.SurfChatApi
import dev.slne.surf.social.api.chat.entry.HistoryEntry
import dev.slne.surf.social.api.chat.entry.HistoryFilter
import dev.slne.surf.social.api.chat.entry.IgnoreListEntry
import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.api.chat.processor.PostChatProcessor
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.api.chat.processor.chatProcessorRegistry
import dev.slne.surf.social.core.common.chat.service.HistoryService
import dev.slne.surf.social.core.common.chat.service.IgnoreService
import dev.slne.surf.social.core.client.chat.redisApi
import dev.slne.surf.social.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.social.core.client.chat.redis.ModerationRedisService
import dev.slne.surf.social.core.client.chat.redis.event.DeleteRemoteMessageRedisEvent
import dev.slne.surf.social.paper.chat.service.SignedMessageSender
import dev.slne.surf.social.paper.plugin
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(SurfChatApi::class)
class PaperSurfChatApiImpl : SurfChatApi, Services.Fallback {
    override suspend fun logMessage(data: MessageData) {
        HistoryService.logMessage(data)
        ModerationRedisService.cache(data)
    }

    override fun deleteMessage(signature: SignedMessage.Signature) = Bukkit.getServer().deleteMessage(signature)

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

    override fun getCachedIgnoreList(uuid: UUID): List<IgnoreListEntry> = IgnoreService.getCachedIgnoreList(uuid)

    override suspend fun getIgnoreList(uuid: UUID): List<IgnoreListEntry> = IgnoreService.loadIgnoreList(uuid)

    override suspend fun lookupHistory(filter: HistoryFilter): ObjectList<HistoryEntry> =
        HistoryService.findHistoryEntry(filter)

    override suspend fun sendSignedMessage(
        signedMessage: SignedMessage,
        senderUuid: UUID,
        targetUuid: UUID,
        contentComponent: Component
    ) {
        val sender = Bukkit.getPlayer(senderUuid)

        if (sender == null) {
            plugin.logger.warning("Tried to send signed message from offline player $senderUuid to $targetUuid!")
            return
        }

        val target = Bukkit.getPlayer(targetUuid)
        if (target != null) {
            SignedMessageSender.sendLocalSignedMessage(sender, target, contentComponent, signedMessage)
        } else {
            SignedMessageSender.sendRemoteSignedMessage(sender, targetUuid, contentComponent, signedMessage)
        }
    }

    override suspend fun passAutoMod(messageData: MessageData) {
        AiModerationPostChatProcessor.processMessage(messageData)
    }
}