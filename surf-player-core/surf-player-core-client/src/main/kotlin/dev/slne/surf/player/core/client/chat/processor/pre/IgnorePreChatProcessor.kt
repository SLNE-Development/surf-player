package dev.slne.surf.player.core.client.chat.processor.pre

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.core.common.chat.service.IgnoreService

object IgnorePreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.IGNORE

    override fun process(context: MessageContext): MessageContext {
        val data = context.messageData
        val receiver = data.receiver

        if (receiver != null && IgnoreService.isIgnored(receiver, data.sender)) {
            context.cancel()
        }

        return context
    }
}
