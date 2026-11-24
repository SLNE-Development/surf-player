package dev.slne.surf.player.core.client.chat.processor

import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The processors of the chat pipeline of this server.
 */
object ChatProcessorRegistry {
    val preChatProcessors = CopyOnWriteArrayList<PreChatProcessor>()
    val postChatProcessors = CopyOnWriteArrayList<PostChatProcessor>()

    private val registrationLock = Any()

    fun clearProcessors() {
        synchronized(registrationLock) {
            preChatProcessors.clear()
            postChatProcessors.clear()
        }
    }

    fun register(preChatProcessor: PreChatProcessor) {
        synchronized(registrationLock) {
            var index = preChatProcessors.size

            for (existing in preChatProcessors.indices) {
                if (preChatProcessors[existing].order > preChatProcessor.order) {
                    index = existing
                    break
                }
            }

            preChatProcessors.add(index, preChatProcessor)
        }
    }

    fun register(postChatProcessor: PostChatProcessor) {
        synchronized(registrationLock) {
            postChatProcessors.add(postChatProcessor)
        }
    }
}
