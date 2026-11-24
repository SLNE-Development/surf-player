package dev.slne.surf.social.microservice.chat

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Schema
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.chat.handler.FunctionalityHandler
import dev.slne.surf.social.microservice.chat.handler.HistoryHandler
import dev.slne.surf.social.microservice.chat.handler.IgnoreListHandler
import dev.slne.surf.social.microservice.chat.rpc.ModerationServiceImpl
import dev.slne.surf.social.microservice.chat.table.FunctionalityTable
import dev.slne.surf.social.microservice.chat.table.HistoryTable
import dev.slne.surf.social.microservice.chat.table.IgnoreListTable
import dev.slne.surf.social.microservice.chat.table.ModerationsTable

object ChatMicroserviceModule {
    /** Must be called inside a transaction. */
    suspend fun createTables() {
        SchemaUtils.createSchema(Schema(SocialSchemas.chat))
        SchemaUtils.create(
            FunctionalityTable,
            HistoryTable,
            IgnoreListTable,
            ModerationsTable
        )
    }

    fun registerHandlers(rabbit: ServerRabbitMQApi) {
        rabbit.registerRequestHandler(HistoryHandler)
        rabbit.registerRequestHandler(FunctionalityHandler)
        rabbit.registerRequestHandler(IgnoreListHandler)

        rabbit.registerRpcService<ModerationService>(ModerationServiceImpl)
    }
}
