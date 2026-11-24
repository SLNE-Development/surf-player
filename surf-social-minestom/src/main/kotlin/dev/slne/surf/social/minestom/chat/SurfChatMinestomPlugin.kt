package dev.slne.surf.social.minestom.chat

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.core.client.chat.config.AiModerationConfig
import dev.slne.surf.social.core.client.chat.config.initChatConfig
import dev.slne.surf.social.core.common.chat.service.FunctionalityService
import dev.slne.surf.social.minestom.chat.command.direct.directMessageCommand
import dev.slne.surf.social.minestom.chat.command.direct.replyCommand
import dev.slne.surf.social.minestom.chat.command.ignore.ignoreCommand
import dev.slne.surf.social.minestom.chat.command.slowChatCommand
import dev.slne.surf.social.minestom.chat.command.spy.directMessageSpyCommand
import dev.slne.surf.social.minestom.chat.command.surfchat.surfChatCommand
import dev.slne.surf.social.minestom.chat.command.teamchatCommand
import dev.slne.surf.social.minestom.chat.listener.ConnectionListener
import dev.slne.surf.social.minestom.chat.redis.listener.MinestomRedisEventListener
import dev.slne.surf.core.api.common.SurfCoreApi

object SurfChatMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        initChatConfig(dataDirectory)
        AiModerationConfig.init(dataDirectory)

        registerProcessors()

        MinestomChatInstance.chatClientLoader.onLoad()

        MinestomChatInstance.redisApi.subscribeToEvents(MinestomRedisEventListener)
        MinestomChatInstance.redisApi.registerRequestHandler(MinestomRedisEventListener)

        MinestomChatInstance.chatClientLoader.onEnable()

        registerChatListener()

        FunctionalityService.fetch(SurfCoreApi.getCurrentServerName())
    }

    override suspend fun onEnable() {
        surfChatCommand()
        teamchatCommand()
        ignoreCommand()
        directMessageSpyCommand()
        slowChatCommand()
        directMessageCommand()
        replyCommand()

        ConnectionListener.register(eventNode)
    }

    override suspend fun onDisable() {
        MinestomChatInstance.chatClientLoader.onDisable()
    }
}
