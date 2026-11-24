package dev.slne.surf.player.microservice.shrieker

import dev.slne.surf.player.microservice.PlayerMicroserviceModule
import dev.slne.surf.player.microservice.PlayerSchemas
import dev.slne.surf.player.microservice.createSchemaWithTables
import dev.slne.surf.player.microservice.shrieker.table.ReportBansTable
import dev.slne.surf.player.microservice.shrieker.table.ReportDataTable
import dev.slne.surf.player.microservice.shrieker.table.ReportsTable

/** Report storage. The microservice keeps the tables; it has no RPC services yet. */
object ShriekerMicroserviceModule : PlayerMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(PlayerSchemas.shrieker, ReportsTable, ReportDataTable, ReportBansTable)
    }
}
