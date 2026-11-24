package dev.slne.surf.social.paper.chat.processor.pre

import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.message.MessageType
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.social.paper.chat.message.MessageFormatter

object FormatPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.FORMAT

    override fun process(context: MessageContext): MessageContext {
        val data = context.messageData

        if (data.type == MessageType.GLOBAL) {
            val hasMention = MessageFormatter.hasMention(data.plainMessage)

            context.render = { viewerUuid, _ ->
                MessageFormatter.formatGlobal(data, viewerUuid, hasMention)
            }
        }

        return context
    }
}
