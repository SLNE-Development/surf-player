package dev.slne.surf.player.microservice.connection

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.player.core.common.connection.rabbit.rpc.SocialConnectionRpc
import dev.slne.surf.player.microservice.PlayerMicroserviceModule
import dev.slne.surf.player.microservice.connection.config.SocialConfig
import dev.slne.surf.player.microservice.connection.rpc.SocialConnectionRpcImpl
import dev.slne.surf.player.microservice.connection.table.AccountsTable
import java.nio.file.Path

/**
 * Social connections (Discord/Twitch account linking), formerly the standalone surf-player
 * microservice.
 */
object ConnectionMicroserviceModule : PlayerMicroserviceModule {
    override fun loadConfig(dataPath: Path) {
        SocialConfig.load(dataPath)
    }

    /** [AccountsTable] lives in the `public` schema owned by the website, so no schema is created. */
    override suspend fun createTables() {
        SchemaUtils.create(AccountsTable)
    }

    override fun registerRpcServices(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<SocialConnectionRpc>(SocialConnectionRpcImpl)
    }
}
