package dev.slne.surf.social.api.chat.processor

import dev.slne.surf.social.api.chat.message.MessageContext

interface PreChatProcessor {
    val order: Int

    fun process(context: MessageContext): MessageContext
    suspend fun processAsync(context: MessageContext): MessageContext = process(context)
}