package dev.slne.surf.social.microservice.chat

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService
import dev.slne.surf.social.microservice.chat.handler.FunctionalityHandler
import dev.slne.surf.social.microservice.chat.handler.HistoryHandler
import dev.slne.surf.social.microservice.chat.handler.IgnoreListHandler
import dev.slne.surf.social.microservice.chat.rpc.ModerationServiceImpl
import dev.slne.surf.social.microservice.chat.table.FunctionalityTable
import dev.slne.surf.social.microservice.chat.table.HistoryTable
import dev.slne.surf.social.microservice.chat.table.IgnoreListTable
import dev.slne.surf.social.microservice.chat.table.ModerationsTable
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import kotlin.io.path.Path

@AutoService(Microservice::class)
class ChatMicroservice : Microservice() {
    override val dataPath = Path("config")
    private val databaseApi = DatabaseApi.create(dataPath)
    private val rabbitApi = ServerRabbitMQApi.create("surf-chat", dataPath)

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(
                FunctionalityTable, HistoryTable, IgnoreListTable, ModerationsTable
            )
        }

        rabbitApi.registerRequestHandler(HistoryHandler)
        rabbitApi.registerRequestHandler(FunctionalityHandler)
        rabbitApi.registerRequestHandler(IgnoreListHandler)

        rabbitApi.registerRpcService<ModerationService>(ModerationServiceImpl)
        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}