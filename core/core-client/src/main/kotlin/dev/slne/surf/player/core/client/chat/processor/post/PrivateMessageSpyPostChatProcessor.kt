package dev.slne.surf.player.core.client.chat.processor.post

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.core.client.chat.message.format.formatPmSpy
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.redis.event.PrivateMessageSpyRedisEvent
import dev.slne.surf.player.core.client.chat.service.SpyService
import dev.slne.surf.player.api.chat.message.MessageData
import java.util.*

/**
 * Shows delivered private messages to the spies of their sender or receiver, wherever in the
 * network those spies are. A private message a pre processor blocked (ignore, filter, ...) was
 * never delivered and is not shown.
 */
object PrivateMessageSpyPostChatProcessor : PostChatProcessor {
    override suspend fun process(messageContext: MessageContext) {
        if (!showsToSpies(messageContext)) {
            return
        }

        val messageData = messageContext.messageData
        val spies = SpyService.spiesOf(messageData.sender, messageData.receiver)

        if (spies.isEmpty()) {
            return
        }

        // Handled by every server including this one, see ChatRedisListener.
        redisApi.publishEvent(PrivateMessageSpyRedisEvent(messageData, spies.toList())).await()
    }

    /** Whether the message of [context] is shown to spies: delivered private messages only. */
    fun showsToSpies(context: MessageContext): Boolean =
        !context.isCancelled && context.messageData.type == MessageType.DIRECT

    /** Shows [messageData] to the [spies] on this server. */
    suspend fun showToLocalSpies(messageData: MessageData, spies: Collection<UUID>) {
        for (spy in spies) {
            if (!ChatPlatform.isOnline(spy)) continue

            ChatPlatform.sendMessage(spy, formatPmSpy(messageData, spy))
        }
    }
}
