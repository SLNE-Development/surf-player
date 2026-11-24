package dev.slne.surf.player.minestom.chat

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.core.client.chat.config.AiModerationConfig
import dev.slne.surf.player.core.client.chat.config.initChatConfig
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.core.common.chat.service.FunctionalityService
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.chat.command.direct.directMessageCommand
import dev.slne.surf.player.minestom.chat.command.direct.replyCommand
import dev.slne.surf.player.minestom.chat.command.ignore.ignoreCommand
import dev.slne.surf.player.minestom.chat.command.slowChatCommand
import dev.slne.surf.player.minestom.chat.command.spy.directMessageSpyCommand
import dev.slne.surf.player.minestom.chat.command.surfchat.surfChatCommand
import dev.slne.surf.player.minestom.chat.command.teamchatCommand
import dev.slne.surf.player.minestom.chat.listener.ChatConnectionHandler
import dev.slne.surf.player.minestom.chat.listener.ChatEventListener
import dev.slne.surf.player.minestom.chat.redis.listener.MinestomRedisEventListener
import net.minestom.server.entity.Player

object ChatMinestomFeature : MinestomPlayerFeature {
    override val id = "chat"

    override suspend fun onLoad() {
        initChatConfig(dataDirectory)
        AiModerationConfig.init(dataDirectory)

        registerProcessors()

        redisApi.subscribeToEvents(MinestomRedisEventListener)
        redisApi.registerRequestHandler(MinestomRedisEventListener)

        ChatEventListener.register()

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
    }

    override fun onPlayerJoin(player: Player) = ChatConnectionHandler.onJoin(player)

    override fun onPlayerQuit(player: Player) = ChatConnectionHandler.onQuit(player)
}
