package dev.slne.surf.player.core.client.chat.processor

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.chatProcessorRegistry

suspend fun runPreProcessors(context: MessageContext): MessageContext {
    var current = context

    for (processor in chatProcessorRegistry.preChatProcessors) {
        current = processor.processAsync(current)

        if (current.isCancelled) {
            return current
        }
    }

    return current
}

suspend fun runPostProcessors(context: MessageContext) {
    for (processor in chatProcessorRegistry.postChatProcessors) {
        processor.process(context)
    }
}
