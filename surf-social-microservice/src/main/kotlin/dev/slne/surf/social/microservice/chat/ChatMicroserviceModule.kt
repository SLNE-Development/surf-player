package dev.slne.surf.social.microservice.chat

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService
import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.chat.handler.FunctionalityHandler
import dev.slne.surf.social.microservice.chat.handler.HistoryHandler
import dev.slne.surf.social.microservice.chat.handler.IgnoreListHandler
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

    override fun registerHandlers(rabbit: ServerRabbitMQApi) {
        rabbit.registerRequestHandler(HistoryHandler)
        rabbit.registerRequestHandler(FunctionalityHandler)
        rabbit.registerRequestHandler(IgnoreListHandler)

        rabbit.registerRpcService<ModerationService>(ModerationServiceImpl)
    }
}
