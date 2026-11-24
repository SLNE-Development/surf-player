package dev.slne.surf.player.microservice.settings

import dev.slne.surf.player.core.common.settings.rpc.SettingsRpcService
import dev.slne.surf.player.microservice.PlayerMicroserviceModule
import dev.slne.surf.player.microservice.PlayerSchemas
import dev.slne.surf.player.microservice.createSchemaWithTables
import dev.slne.surf.player.microservice.settings.rpc.SettingsRpcServiceImpl
import dev.slne.surf.player.microservice.settings.table.SettingEntriesTable
import dev.slne.surf.player.microservice.settings.table.SettingsTable
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi

object SettingsMicroserviceModule : PlayerMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(PlayerSchemas.settings, SettingsTable, SettingEntriesTable)
    }

    override fun registerRpcServices(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<SettingsRpcService>(SettingsRpcServiceImpl)
    }
}
