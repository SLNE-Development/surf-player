package dev.slne.surf.social.microservice.connection

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc
import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.connection.config.SocialConfig
import dev.slne.surf.social.microservice.connection.handler.SocialConnectionsHandler
import dev.slne.surf.social.microservice.connection.rpc.SocialConnectionRpcImpl
import dev.slne.surf.social.microservice.connection.table.AccountsTable
import java.nio.file.Path

/**
 * Social connections (Discord/Twitch account linking), formerly the standalone surf-social
 * microservice.
 */
object ConnectionMicroserviceModule : SocialMicroserviceModule {
    override fun loadConfig(dataPath: Path) {
        SocialConfig.load(dataPath)
    }

    /** [AccountsTable] lives in the `public` schema owned by the website, so no schema is created. */
    override suspend fun createTables() {
        SchemaUtils.create(AccountsTable)
    }

    override fun registerHandlers(rabbit: ServerRabbitMQApi) {
        rabbit.registerRequestHandler(SocialConnectionsHandler)
        rabbit.registerRpcService<SocialConnectionRpc>(SocialConnectionRpcImpl)
    }
}
