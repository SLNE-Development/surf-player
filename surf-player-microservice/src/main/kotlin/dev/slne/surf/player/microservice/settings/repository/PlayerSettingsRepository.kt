package dev.slne.surf.player.microservice.settings.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.player.microservice.settings.table.SettingEntriesTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

object PlayerSettingsRepository {
    suspend fun loadSettingsByPlayerUuid(playerUuid: UUID): Map<String, String> = suspendTransaction {
        SettingEntriesTable
            .select(SettingEntriesTable.settingName, SettingEntriesTable.value)
            .where(SettingEntriesTable.playerUuid eq playerUuid)
            .map { it[SettingEntriesTable.settingName] to it[SettingEntriesTable.value] }
            .toList()
            .toMap()
    }

    suspend fun savePlayerSetting(playerUuid: UUID, settingName: String, value: String) {
        suspendTransaction {
            SettingEntriesTable.upsert(
                SettingEntriesTable.settingName,
                SettingEntriesTable.playerUuid
            ) {
                it[SettingEntriesTable.playerUuid] = playerUuid
                it[SettingEntriesTable.settingName] = settingName
                it[SettingEntriesTable.value] = value
            }
        }
    }
}
