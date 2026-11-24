package dev.slne.surf.social.core.client.chat.processor.pre

import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.social.core.common.chat.service.IgnoreService

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
