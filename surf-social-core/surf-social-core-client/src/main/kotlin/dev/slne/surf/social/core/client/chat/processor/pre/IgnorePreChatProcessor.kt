package dev.slne.surf.social.core.client.chat.processor.pre

import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.social.core.common.chat.service.IgnoreService
import java.util.UUID

object IgnorePreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.IGNORE

    override fun process(context: MessageContext): MessageContext {
        val data = context.messageData

        if (isIgnored(data.receiver, data.sender)) {
            context.cancel()
            return context
        }

        return context
    }

    private fun isIgnored(receiver: UUID?, sender: UUID): Boolean {
        if (receiver == null) {
            return false
        }

        return IgnoreService.isIgnored(receiver, sender)
    }
}
