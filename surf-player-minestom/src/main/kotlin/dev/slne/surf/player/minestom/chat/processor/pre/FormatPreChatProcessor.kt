package dev.slne.surf.social.minestom.chat.processor.pre

import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.message.MessageType
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.social.minestom.chat.message.MinestomMessageFormatter

object FormatPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.FORMAT

    override fun process(context: MessageContext): MessageContext {
        val data = context.messageData

        if (data.type == MessageType.GLOBAL) {
            val hasMention = MinestomMessageFormatter.hasMention(data.plainMessage)

            context.render = { viewerUuid, _ ->
                MinestomMessageFormatter.formatGlobal(data, viewerUuid, hasMention)
            }
        }

        return context
    }
}
