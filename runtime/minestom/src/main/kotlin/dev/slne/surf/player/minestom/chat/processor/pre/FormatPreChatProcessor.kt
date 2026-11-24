package dev.slne.surf.player.minestom.chat.processor.pre

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.minestom.chat.message.MinestomMessageFormatter

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
