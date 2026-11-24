package dev.slne.surf.social.microservice.shrieker

import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.createSchemaWithTables
import dev.slne.surf.social.microservice.shrieker.table.ReportBansTable
import dev.slne.surf.social.microservice.shrieker.table.ReportDataTable
import dev.slne.surf.social.microservice.shrieker.table.ReportsTable

/** Report storage. The microservice keeps the tables; it has no request handlers yet. */
object ShriekerMicroserviceModule : SocialMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(SocialSchemas.shrieker, ReportsTable, ReportDataTable, ReportBansTable)
    }
}
