package dev.slne.surf.player.microservice.chat

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.player.core.common.chat.rpc.ChatFunctionalityRpcService
import dev.slne.surf.player.core.common.chat.rpc.ChatHistoryRpcService
import dev.slne.surf.player.core.common.chat.rpc.ChatIgnoreRpcService
import dev.slne.surf.player.core.common.chat.rpc.ChatModerationRpcService
import dev.slne.surf.player.microservice.PlayerMicroserviceModule
import dev.slne.surf.player.microservice.PlayerSchemas
import dev.slne.surf.player.microservice.chat.rpc.ChatFunctionalityRpcServiceImpl
import dev.slne.surf.player.microservice.chat.rpc.ChatHistoryRpcServiceImpl
import dev.slne.surf.player.microservice.chat.rpc.ChatIgnoreRpcServiceImpl
import dev.slne.surf.player.microservice.chat.rpc.ChatModerationRpcServiceImpl
import dev.slne.surf.player.microservice.chat.table.FunctionalityTable
import dev.slne.surf.player.microservice.chat.table.HistoryTable
import dev.slne.surf.player.microservice.chat.table.IgnoreListTable
import dev.slne.surf.player.microservice.chat.table.ModerationsTable
import dev.slne.surf.player.microservice.createSchemaWithTables

object ChatMicroserviceModule : PlayerMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(
            PlayerSchemas.chat,
            FunctionalityTable,
            HistoryTable,
            IgnoreListTable,
            ModerationsTable
        )
    }

    override fun registerRpcServices(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<ChatHistoryRpcService>(ChatHistoryRpcServiceImpl)
        rabbit.registerRpcService<ChatFunctionalityRpcService>(ChatFunctionalityRpcServiceImpl)
        rabbit.registerRpcService<ChatIgnoreRpcService>(ChatIgnoreRpcServiceImpl)
        rabbit.registerRpcService<ChatModerationRpcService>(ChatModerationRpcServiceImpl)
    }
}
