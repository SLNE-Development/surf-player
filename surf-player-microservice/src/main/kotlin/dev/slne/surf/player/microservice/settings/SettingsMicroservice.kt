package dev.slne.surf.social.microservice.settings

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.microservice.settings.rabbit.handler.PlayerSettingsHandler
import dev.slne.surf.social.microservice.settings.rabbit.handler.SettingsHandler
import dev.slne.surf.social.microservice.settings.table.SettingEntriesTable
import dev.slne.surf.social.microservice.settings.table.SettingsTable
import kotlin.io.path.Path

@AutoService(Microservice::class)
class SettingsMicroservice : Microservice() {
    override val dataPath = Path("config")
    private val databaseApi = DatabaseApi.create(dataPath)
    private val rabbitApi = ServerRabbitMQApi.create("surf-settings", dataPath)

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(
                SettingEntriesTable,
                SettingsTable
            )
        }

        rabbitApi.registerRequestHandler(SettingsHandler)
        rabbitApi.registerRequestHandler(PlayerSettingsHandler)
        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}