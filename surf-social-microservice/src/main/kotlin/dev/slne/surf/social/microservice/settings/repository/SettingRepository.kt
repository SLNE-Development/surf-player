package dev.slne.surf.social.microservice.settings.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.social.api.settings.setting.Setting
import dev.slne.surf.social.microservice.settings.table.SettingsTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

val settingRepository = SettingRepository()

class SettingRepository {
    suspend fun loadSettings(): List<Setting> = suspendTransaction {
        SettingsTable.select(SettingsTable.name, SettingsTable.defaultValue).map {
            Setting(
                name = it[SettingsTable.name],
                defaultValue = it[SettingsTable.defaultValue]
            )
        }.toList()
    }

    suspend fun createSetting(name: String, defaultValue: String): Setting = suspendTransaction {
        SettingsTable.upsert(SettingsTable.name) {
            it[SettingsTable.name] = name
            it[SettingsTable.defaultValue] = defaultValue
        }

        Setting(name, defaultValue)
    }

    suspend fun deleteSetting(name: String) = suspendTransaction {
        SettingsTable.deleteWhere { SettingsTable.name eq name }
    }
}
