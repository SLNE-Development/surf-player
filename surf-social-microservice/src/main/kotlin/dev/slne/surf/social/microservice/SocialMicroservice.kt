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
import dev.slne.surf.social.microservice.clan.clanMicroservice
import dev.slne.surf.social.microservice.friends.FriendsMicroserviceModule
import dev.slne.surf.social.microservice.shrieker.ShriekerMicroserviceModule
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * The single surf-social microservice. Hosts chat, clan, friends and shrieker on one database
 * connection and one RabbitMQ connection named [SOCIAL_CONNECTION_NAME].
 */
@AutoService(Microservice::class)
class SocialMicroservice : Microservice() {
    override val dataPath: Path = Path("config")
    val databaseApi = DatabaseApi.create(dataPath)
    val rabbitApi = ServerRabbitMQApi.create(
        SOCIAL_CONNECTION_NAME,
        dataPath,
        SocialSerializerModule.module
    )

    init {
        clanMicroservice = this
    }

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            ChatMicroserviceModule.createTables()
            ClanMicroserviceModule.createTables()
            FriendsMicroserviceModule.createTables()
            ShriekerMicroserviceModule.createTables()
        }

        ClanMicroserviceModule.load()

        ChatMicroserviceModule.registerHandlers(rabbitApi)
        ClanMicroserviceModule.registerHandlers(rabbitApi)
        FriendsMicroserviceModule.registerHandlers(rabbitApi)
        ShriekerMicroserviceModule.registerHandlers(rabbitApi)

        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        ClanMicroserviceModule.disable()
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}
