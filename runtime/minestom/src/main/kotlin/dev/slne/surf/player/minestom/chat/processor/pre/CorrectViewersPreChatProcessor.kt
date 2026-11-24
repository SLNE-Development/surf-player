package dev.slne.surf.player.minestom.chat.processor.pre

import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.core.client.chat.service.IgnoreService
import net.kyori.adventure.audience.Audience
import net.minestom.server.command.ConsoleSender
import java.util.*

object CorrectViewersPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.CORRECT_VIEWERS

    override fun process(context: MessageContext): MessageContext {
        val sender = context.messageData.sender
        context.viewers.removeIf { it.isConsole() || isIgnored(it.uuidOrNull(), sender) }

        return context
    }

    private fun Audience.isConsole() = this is ConsoleSender

    private fun isIgnored(viewer: UUID?, sender: UUID): Boolean {
        return viewer != null && IgnoreService.isIgnored(viewer, sender)
    }
}
