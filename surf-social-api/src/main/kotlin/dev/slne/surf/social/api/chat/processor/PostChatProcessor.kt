package dev.slne.surf.social.api.chat.processor

import dev.slne.surf.social.api.chat.message.MessageContext

fun interface PostChatProcessor {
    suspend fun process(messageContext: MessageContext)
}