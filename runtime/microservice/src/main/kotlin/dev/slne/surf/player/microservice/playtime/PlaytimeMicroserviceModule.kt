package dev.slne.surf.player.microservice.playtime

import dev.slne.surf.player.core.common.playtime.rpc.PlaytimeRpcService
import dev.slne.surf.player.microservice.PlayerMicroserviceModule
import dev.slne.surf.player.microservice.PlayerSchemas
import dev.slne.surf.player.microservice.createSchemaWithTables
import dev.slne.surf.player.microservice.playtime.db.table.PlaytimeSessionsTable
import dev.slne.surf.player.microservice.playtime.db.table.PlaytimeStreakPausesTable
import dev.slne.surf.player.microservice.playtime.db.table.PlaytimeStreaksTable
import dev.slne.surf.player.microservice.playtime.rpc.PlaytimeRpcServiceImpl
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi

object PlaytimeMicroserviceModule : PlayerMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(
            PlayerSchemas.playtime,
            PlaytimeSessionsTable,
            PlaytimeStreaksTable,
            PlaytimeStreakPausesTable
        )
    }

    override fun registerRpcServices(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<PlaytimeRpcService>(PlaytimeRpcServiceImpl)
    }
}
