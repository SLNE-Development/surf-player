package dev.slne.surf.social.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.SOCIAL_CONNECTION_NAME
import dev.slne.surf.social.core.common.SocialSerializerModule
import dev.slne.surf.social.microservice.chat.ChatMicroserviceModule
import dev.slne.surf.social.microservice.clan.ClanMicroserviceModule
import dev.slne.surf.social.microservice.connection.ConnectionMicroserviceModule
import dev.slne.surf.social.microservice.friends.FriendsMicroserviceModule
import dev.slne.surf.social.microservice.shrieker.ShriekerMicroserviceModule
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * The single surf-social microservice. Hosts chat, clan, friends, shrieker and social connections
 * on one database connection and one RabbitMQ connection named [SOCIAL_CONNECTION_NAME].
 */
@AutoService(Microservice::class)
class SocialMicroservice : Microservice() {
    override val dataPath: Path = Path("config")
    private val databaseApi = DatabaseApi.create(dataPath)
    private val rabbitApi = ServerRabbitMQApi.create(
        SOCIAL_CONNECTION_NAME,
        dataPath,
        SocialSerializerModule.module
    )

    private val modules: List<SocialMicroserviceModule> = listOf(
        ChatMicroserviceModule,
        ClanMicroserviceModule,
        FriendsMicroserviceModule,
        ShriekerMicroserviceModule,
        ConnectionMicroserviceModule
    )

    override suspend fun onBootstrap(args: List<String>) {
        modules.forEach { it.loadConfig(dataPath) }

        suspendTransaction {
            modules.forEach { it.createTables() }
        }

        modules.forEach { it.registerHandlers(rabbitApi) }
        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}
