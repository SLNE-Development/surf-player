package dev.slne.surf.social.paper.chat

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.social.api.chat.processor.chatProcessorRegistry
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.client.chat.config.AiModerationConfig
import dev.slne.surf.social.core.client.chat.config.initChatConfig
import dev.slne.surf.social.core.common.chat.service.FunctionalityService
import dev.slne.surf.social.paper.chat.command.direct.directMessageCommand
import dev.slne.surf.social.paper.chat.command.direct.replyCommand
import dev.slne.surf.social.paper.chat.command.ignore.ignoreCommand
import dev.slne.surf.social.paper.chat.command.slowchat.slowChatCommand
import dev.slne.surf.social.paper.chat.command.spy.directMessageSpyCommand
import dev.slne.surf.social.paper.chat.command.surfchat.surfChatCommand
import dev.slne.surf.social.paper.chat.command.teamchatCommand
import dev.slne.surf.social.paper.chat.listener.AsyncChatListener
import dev.slne.surf.social.paper.chat.listener.ConnectListener
import dev.slne.surf.social.paper.chat.listener.DisconnectListener
import dev.slne.surf.social.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.social.core.client.chat.processor.post.LogPostChatProcessor
import dev.slne.surf.social.core.client.chat.processor.post.PrivateMessageSpyPostChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.IgnorePreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.SlowChatPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.ValidatorPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.validate.CharPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.validate.LinkPreChatProcessor
import dev.slne.surf.social.core.client.chat.processor.pre.validate.SpamPreChatProcessor
import dev.slne.surf.social.paper.chat.processor.pre.CorrectViewersPreChatProcessor
import dev.slne.surf.social.paper.chat.processor.pre.FormatPreChatProcessor
import dev.slne.surf.social.paper.chat.redis.listener.RedisEventListener
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.social.paper.PaperSocialFeature

object PaperChatFeature : PaperSocialFeature {
    override val name = "chat"

    override suspend fun onLoad() {
        initChatConfig(dataPath)
        AiModerationConfig.init(dataPath)

        SocialClient.redisApi.subscribeToEvents(RedisEventListener)
        SocialClient.redisApi.registerRequestHandler(RedisEventListener)

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

    override suspend fun onEnable() {
        surfChatCommand()
        teamchatCommand()
        ignoreCommand()
        directMessageSpyCommand()
        directMessageCommand()
        replyCommand()
        slowChatCommand()

        AsyncChatListener.register()
        DisconnectListener.register()
        ConnectListener.register()

        FunctionalityService.fetch(SurfCoreApi.getCurrentServerName())
    }
}
