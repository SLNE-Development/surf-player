package dev.slne.surf.social.microservice.shrieker

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.microservice.shrieker.handler.ReportBansHandler
import dev.slne.surf.social.microservice.shrieker.handler.ReportsHandler
import dev.slne.surf.social.microservice.shrieker.handler.VoiceLogHandler
import dev.slne.surf.social.microservice.shrieker.table.ReportBansTable
import dev.slne.surf.social.microservice.shrieker.table.ReportDataTable
import dev.slne.surf.social.microservice.shrieker.table.ReportsTable
import kotlin.io.path.Path

@AutoService(Microservice::class)
class ShriekerMicroservice : Microservice() {
    override val dataPath = Path("config")
    private val databaseApi = DatabaseApi.create(dataPath)
    private val rabbitApi = ServerRabbitMQApi.create("surf-shrieker", dataPath)

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(
                ReportsTable,
                ReportDataTable,
                ReportBansTable
            )
        }

        rabbitApi.registerRequestHandler(VoiceLogHandler)
        rabbitApi.registerRequestHandler(ReportsHandler)
        rabbitApi.registerRequestHandler(ReportBansHandler)

        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}