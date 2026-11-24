package dev.slne.surf.social.microservice.shrieker.table

import dev.slne.surf.api.core.messages.adventure.toKey
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import dev.slne.surf.social.microservice.SocialSchemas

object ReportDataTable : ULongIdTable("${SocialSchemas.shrieker}.shrieker_data") {
    val reportId = long("report_id").references(ReportsTable.id)
    val key = varchar("key", 255).transform({ it.toKey() }, { it.toString() })
    val value = text("value")
}