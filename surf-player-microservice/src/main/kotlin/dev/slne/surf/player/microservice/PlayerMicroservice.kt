package dev.slne.surf.player.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.player.core.common.PLAYER_CONNECTION_NAME
import dev.slne.surf.player.core.common.PlayerSerializerModule
import dev.slne.surf.player.microservice.chat.ChatMicroserviceModule
import dev.slne.surf.player.microservice.clan.ClanMicroserviceModule
import dev.slne.surf.player.microservice.connection.ConnectionMicroserviceModule
import dev.slne.surf.player.microservice.friends.FriendsMicroserviceModule
import dev.slne.surf.player.microservice.playtime.PlaytimeMicroserviceModule
import dev.slne.surf.player.microservice.settings.SettingsMicroserviceModule
import dev.slne.surf.player.microservice.shrieker.ShriekerMicroserviceModule
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * The single surf-player microservice. Hosts chat, clan, friends, shrieker and social connections
 * on one database connection and one RabbitMQ connection named [PLAYER_CONNECTION_NAME].
 */
@AutoService(Microservice::class)
class PlayerMicroservice : Microservice() {
    override val dataPath: Path = Path("config")
    private val databaseApi = DatabaseApi.create(dataPath)
    private val rabbitApi = ServerRabbitMQApi.create(
        PLAYER_CONNECTION_NAME,
        dataPath,
        PlayerSerializerModule.module
    )

    private val modules: List<PlayerMicroserviceModule> = listOf(
        SettingsMicroserviceModule,
        PlaytimeMicroserviceModule,
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

        modules.forEach { it.registerRpcServices(rabbitApi) }
        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}
