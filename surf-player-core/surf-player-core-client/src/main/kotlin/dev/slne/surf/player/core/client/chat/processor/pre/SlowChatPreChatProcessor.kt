package dev.slne.surf.player.core.client.chat.processor.pre

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.core.client.chat.service.SlowChatService
import dev.slne.surf.player.core.client.chat.util.sendText

object SlowChatPreChatProcessor : PreChatProcessor {
    override val order: Int = ProcessorOrder.SLOW_CHAT

    override fun process(context: MessageContext): MessageContext {
        if (context.messageData.type != MessageType.GLOBAL) {
            return context
        }

        val sender = context.messageData.sender

        if (!ChatPlatform.isOnline(sender)) {
            return context
        }

        if (!SlowChatService.checkPlayer(sender)) {
            sender.sendText {
                appendErrorPrefix()
                error("Bitte warte einen Moment bevor du erneut eine Nachricht sendest.")
            }
            context.cancel()
            return context
        }

        return context
    }
}
