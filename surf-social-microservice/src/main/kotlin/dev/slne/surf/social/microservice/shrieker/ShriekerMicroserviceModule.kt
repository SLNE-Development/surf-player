package dev.slne.surf.social.microservice.shrieker

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Schema
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.shrieker.handler.ReportBansHandler
import dev.slne.surf.social.microservice.shrieker.handler.ReportsHandler
import dev.slne.surf.social.microservice.shrieker.handler.VoiceLogHandler
import dev.slne.surf.social.microservice.shrieker.table.ReportBansTable
import dev.slne.surf.social.microservice.shrieker.table.ReportDataTable
import dev.slne.surf.social.microservice.shrieker.table.ReportsTable

object ShriekerMicroserviceModule {
    /** Must be called inside a transaction. */
    suspend fun createTables() {
        SchemaUtils.createSchema(Schema(SocialSchemas.shrieker))
        SchemaUtils.create(
            ReportsTable,
            ReportDataTable,
            ReportBansTable
        )
    }

    fun registerHandlers(rabbit: ServerRabbitMQApi) {
        rabbit.registerRequestHandler(VoiceLogHandler)
        rabbit.registerRequestHandler(ReportsHandler)
        rabbit.registerRequestHandler(ReportBansHandler)
    }
}
