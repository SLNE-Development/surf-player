package dev.slne.surf.player.microservice.settings.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.microservice.settings.table.SettingsTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

object SettingRepository {
    suspend fun loadSettings(): List<Setting> = suspendTransaction {
        SettingsTable.select(SettingsTable.name, SettingsTable.defaultValue).map {
            Setting(
                name = it[SettingsTable.name],
                defaultValue = it[SettingsTable.defaultValue]
            )
        }.toList()
    }

    suspend fun createSettings(defaultValues: Map<String, String>): List<Setting> = suspendTransaction {
        defaultValues.map { (name, defaultValue) ->
            SettingsTable.upsert(SettingsTable.name) {
                it[SettingsTable.name] = name
                it[SettingsTable.defaultValue] = defaultValue
            }

            Setting(name, defaultValue)
        }
    }
}
