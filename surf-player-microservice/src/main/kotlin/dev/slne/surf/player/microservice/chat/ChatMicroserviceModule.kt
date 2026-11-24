package dev.slne.surf.social.microservice.chat

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatFunctionalityRpcService
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatHistoryRpcService
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatIgnoreRpcService
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService
import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.chat.rpc.ChatFunctionalityRpcServiceImpl
import dev.slne.surf.social.microservice.chat.rpc.ChatHistoryRpcServiceImpl
import dev.slne.surf.social.microservice.chat.rpc.ChatIgnoreRpcServiceImpl
import dev.slne.surf.social.microservice.chat.rpc.ModerationServiceImpl
import dev.slne.surf.social.microservice.chat.table.FunctionalityTable
import dev.slne.surf.social.microservice.chat.table.HistoryTable
import dev.slne.surf.social.microservice.chat.table.IgnoreListTable
import dev.slne.surf.social.microservice.chat.table.ModerationsTable
import dev.slne.surf.social.microservice.createSchemaWithTables

object ChatMicroserviceModule : SocialMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(
            SocialSchemas.chat,
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
        rabbit.registerRpcService<ModerationService>(ModerationServiceImpl)
    }
}
