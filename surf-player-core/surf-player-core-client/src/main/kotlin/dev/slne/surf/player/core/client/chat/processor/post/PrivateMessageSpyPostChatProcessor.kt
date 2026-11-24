package dev.slne.surf.player.core.client.chat.processor.post

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import dev.slne.surf.player.core.client.chat.message.format.formatPmSpy
import dev.slne.surf.player.core.client.chat.util.sendText
import dev.slne.surf.player.core.common.chat.service.SpyService

object PrivateMessageSpyPostChatProcessor : PostChatProcessor {
    override suspend fun process(messageContext: MessageContext) {
        val messageData = messageContext.messageData

        if (messageData.type != MessageType.DIRECT) {
            return
        }

        val spies = SpyService.getObservingPlayers(messageData.sender)

        if (spies.isEmpty()) {
            return
        }

        val message = formatPmSpy(messageData)

        for (spy in spies) {
            spy.sendText { append(message) }
        }
    }
}
