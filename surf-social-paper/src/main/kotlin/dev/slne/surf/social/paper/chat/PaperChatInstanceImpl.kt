package dev.slne.surf.social.paper.chat

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.chat.ClientChatInstance
import dev.slne.surf.social.core.common.chat.ChatInstance
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService
import net.kyori.adventure.util.Services

@AutoService(ChatInstance::class)
class PaperChatInstanceImpl : ClientChatInstance, Services.Fallback {
    override val moderationService by lazy {
        rabbitApi.createRpcService<ModerationService>()
    }
}