package dev.slne.surf.player.api.chat.processor

import dev.slne.surf.player.api.chat.message.MessageContext

interface PreChatProcessor {
    val order: Int

    fun process(context: MessageContext): MessageContext
    suspend fun processAsync(context: MessageContext): MessageContext = process(context)
}