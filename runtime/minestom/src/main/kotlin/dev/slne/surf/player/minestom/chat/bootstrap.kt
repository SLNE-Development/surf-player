package dev.slne.surf.player.minestom.chat

import dev.slne.surf.player.core.client.chat.processor.ChatProcessorRegistry
import dev.slne.surf.player.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.player.core.client.chat.processor.post.LogPostChatProcessor
import dev.slne.surf.player.core.client.chat.processor.post.PrivateMessageSpyPostChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.IgnorePreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.SlowChatPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.ValidatorPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.validate.CharPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.validate.LinkPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.validate.SpamPreChatProcessor
import dev.slne.surf.player.minestom.chat.processor.pre.CorrectViewersPreChatProcessor
import dev.slne.surf.player.minestom.chat.processor.pre.FormatPreChatProcessor

/**
 * Registers the chat processors that make up the chat pipeline of this platform.
 */
fun registerProcessors() {
    ChatProcessorRegistry.register(CharPreChatProcessor)
    ChatProcessorRegistry.register(LinkPreChatProcessor)
    ChatProcessorRegistry.register(SpamPreChatProcessor)
    ChatProcessorRegistry.register(CorrectViewersPreChatProcessor)
    ChatProcessorRegistry.register(FormatPreChatProcessor)
    ChatProcessorRegistry.register(IgnorePreChatProcessor)
    ChatProcessorRegistry.register(ValidatorPreChatProcessor)
    ChatProcessorRegistry.register(SlowChatPreChatProcessor)

    ChatProcessorRegistry.register(LogPostChatProcessor)
    ChatProcessorRegistry.register(AiModerationPostChatProcessor)
    ChatProcessorRegistry.register(PrivateMessageSpyPostChatProcessor)
}
