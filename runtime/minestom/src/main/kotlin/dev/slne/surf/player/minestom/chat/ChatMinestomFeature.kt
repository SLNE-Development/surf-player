package dev.slne.surf.player.minestom.chat

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.core.client.chat.config.AiModerationConfig
import dev.slne.surf.player.core.client.chat.config.initChatConfig
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.core.client.chat.redis.ChatRedisListener
import dev.slne.surf.player.core.client.chat.service.FunctionalityService
import dev.slne.surf.player.core.client.chat.service.SpyService
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.chat.command.chatAdminActions
import dev.slne.surf.player.minestom.chat.command.registerChatCommands
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

        SpyService.register(redisApi)
        redisApi.subscribeToEvents(ChatRedisListener)
        redisApi.registerRequestHandler(MinestomRedisEventListener)

        ChatEventListener.register()

        FunctionalityService.fetch(SurfCoreApi.getCurrentServerName())
    }

    override suspend fun onEnable() {
        registerChatCommands()
    }

    override fun adminActions() = chatAdminActions()

    override fun onPlayerJoin(player: Player) = ChatConnectionHandler.onJoin(player)

    override fun onPlayerQuit(player: Player) = ChatConnectionHandler.onQuit(player)
}
