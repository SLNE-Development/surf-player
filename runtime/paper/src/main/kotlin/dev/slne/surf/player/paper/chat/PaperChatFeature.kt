package dev.slne.surf.player.paper.chat

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.chat.processor.ChatProcessorRegistry
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.chat.config.AiModerationConfig
import dev.slne.surf.player.core.client.chat.config.initChatConfig
import dev.slne.surf.player.core.client.chat.redis.ChatRedisListener
import dev.slne.surf.player.core.client.chat.service.FunctionalityService
import dev.slne.surf.player.paper.chat.command.chatAdminActions
import dev.slne.surf.player.paper.chat.command.directMessageCommand
import dev.slne.surf.player.paper.chat.command.ignoreCommand
import dev.slne.surf.player.paper.chat.command.replyCommand
import dev.slne.surf.player.paper.chat.command.slowChatCommand
import dev.slne.surf.player.paper.chat.command.spyCommand
import dev.slne.surf.player.paper.chat.command.teamchatCommand
import dev.slne.surf.player.paper.chat.listener.AsyncChatListener
import dev.slne.surf.player.paper.chat.listener.ConnectListener
import dev.slne.surf.player.paper.chat.listener.DisconnectListener
import dev.slne.surf.player.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.player.core.client.chat.processor.post.LogPostChatProcessor
import dev.slne.surf.player.core.client.chat.processor.post.PrivateMessageSpyPostChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.IgnorePreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.SlowChatPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.ValidatorPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.validate.CharPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.validate.LinkPreChatProcessor
import dev.slne.surf.player.core.client.chat.processor.pre.validate.SpamPreChatProcessor
import dev.slne.surf.player.paper.chat.processor.pre.CorrectViewersPreChatProcessor
import dev.slne.surf.player.paper.chat.processor.pre.FormatPreChatProcessor
import dev.slne.surf.player.paper.chat.redis.listener.RedisEventListener
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.paper.PaperPlayerFeature

object PaperChatFeature : PaperPlayerFeature {
    override val name = "chat"

    override suspend fun onLoad() {
        initChatConfig(dataPath)
        AiModerationConfig.init(dataPath)

        SurfPlayerClient.redisApi.subscribeToEvents(ChatRedisListener)
        SurfPlayerClient.redisApi.registerRequestHandler(RedisEventListener)

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

    override suspend fun onEnable() {
        teamchatCommand()
        ignoreCommand()
        spyCommand()
        directMessageCommand()
        replyCommand()
        slowChatCommand()

        AsyncChatListener.register()
        DisconnectListener.register()
        ConnectListener.register()

        FunctionalityService.fetch(SurfCoreApi.getCurrentServerName())
    }

    override fun adminActions() = chatAdminActions()
}
