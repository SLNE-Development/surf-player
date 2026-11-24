package dev.slne.surf.player.microservice.settings.table

import dev.slne.surf.database.table.AuditableLongIdTable

object SettingsTable : AuditableLongIdTable("settings_settings") {
    val name = varchar("name", 255).uniqueIndex()
    val defaultValue = text("default_value")
}