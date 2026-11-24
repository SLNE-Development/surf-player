package dev.slne.surf.social.minestom.chat

import dev.slne.surf.social.api.chat.processor.chatProcessorRegistry
import dev.slne.surf.social.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.social.core.client.chat.processor.post.LogPostChatProcessor
import dev.slne.surf.social.core.client.chat.processor.post.PrivateMessageSpyPostChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.IgnorePreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.SlowChatPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.ValidatorPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.validate.CharPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.validate.LinkPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.validate.SpamPreChatProcessor
import dev.slne.surf.social.minestom.chat.processor.pre.CorrectViewersPreChatProcessor
import dev.slne.surf.social.minestom.chat.processor.pre.FormatPreChatProcessor

/**
 * Registers the chat processors that make up the chat pipeline of this platform.
 */
fun registerProcessors() {
    chatProcessorRegistry.register(CharPreChatProcessor)
    chatProcessorRegistry.register(LinkPreChatProcessor)
    chatProcessorRegistry.register(SpamPreChatProcessor)
    chatProcessorRegistry.register(CorrectViewersPreChatProcessor)
    chatProcessorRegistry.register(FormatPreChatProcessor)
    chatProcessorRegistry.register(IgnorePreChatProcessor)
    chatProcessorRegistry.register(ValidatorPreChatProcessor)
    chatProcessorRegistry.register(SlowChatPreChatProcessor)

    chatProcessorRegistry.register(LogPostChatProcessor)
    chatProcessorRegistry.register(AiModerationPostChatProcessor)
    chatProcessorRegistry.register(PrivateMessageSpyPostChatProcessor)
}
