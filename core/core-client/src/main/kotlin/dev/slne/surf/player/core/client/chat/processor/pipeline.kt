package dev.slne.surf.player.core.client.chat.processor

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import kotlin.coroutines.cancellation.CancellationException

private val log = logger()

suspend fun runPreProcessors(context: MessageContext): MessageContext {
    var current = context

    for (processor in ChatProcessorRegistry.preChatProcessors) {
        current = processor.processAsync(current)

        if (current.isCancelled) {
            return current
        }
    }

    return current
}

suspend fun runPostProcessors(context: MessageContext) {
    runPostProcessors(context, ChatProcessorRegistry.postChatProcessors)
}

/**
 * Runs [processors] on [context] in isolation: the message was already delivered when post
 * processors run, so a processor that fails (e.g. the AI moderation without a reachable API) is
 * logged and neither keeps the remaining processors from running nor fails the command or chat
 * event that sent the message.
 */
suspend fun runPostProcessors(context: MessageContext, processors: Iterable<PostChatProcessor>) {
    for (processor in processors) {
        try {
            processor.process(context)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Post chat processor %s failed for message %s", processor.javaClass.name, context.messageData.messageUuid)
        }
    }
}
