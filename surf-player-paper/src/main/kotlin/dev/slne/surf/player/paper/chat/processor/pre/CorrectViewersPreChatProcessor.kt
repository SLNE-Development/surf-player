package dev.slne.surf.social.paper.chat.processor.pre

import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.core.common.chat.service.IgnoreService
import dev.slne.surf.social.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.social.paper.chat.util.isConsole
import java.util.*

object CorrectViewersPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.CORRECT_VIEWERS

    override fun process(context: MessageContext): MessageContext {
        val sender = context.messageData.sender
        context.viewers.removeIf { it.isConsole() || isIgnored(it.uuidOrNull(), sender) }

        return context
    }

    private fun isIgnored(viewer: UUID?, sender: UUID): Boolean {
        return viewer != null && IgnoreService.isIgnored(viewer, sender)
    }
}