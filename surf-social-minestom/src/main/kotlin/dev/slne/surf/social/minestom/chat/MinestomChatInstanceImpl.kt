package dev.slne.surf.social.minestom.chat

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.chat.ChatClientLoader
import dev.slne.surf.social.core.client.chat.ClientChatInstance
import dev.slne.surf.social.core.common.chat.ChatInstance
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService
import net.kyori.adventure.util.Services

/**
 * Platform local name for the chat instance backing the chat feature.
 */
typealias MinestomChatInstance = ClientChatInstance

@AutoService(ChatInstance::class)
class MinestomChatInstanceImpl : ClientChatInstance, Services.Fallback {
    override val chatClientLoader = ChatClientLoader(ChatMinestomFeature.dataDirectory)
    override val moderationService by lazy {
        rabbitApi.createRpcService<ModerationService>()
    }
}
