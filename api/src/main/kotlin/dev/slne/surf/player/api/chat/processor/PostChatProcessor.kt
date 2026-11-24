package dev.slne.surf.player.api.chat.processor

import dev.slne.surf.player.api.chat.message.MessageContext

fun interface PostChatProcessor {
    suspend fun process(messageContext: MessageContext)
}