package dev.slne.surf.player.microservice.settings.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.player.microservice.settings.table.SettingEntriesTable
import dev.slne.surf.player.microservice.settings.table.SettingsTable
import kotlinx.coroutines.flow.firstOrNull
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

    /**
     * The value [playerUuid] chose for [settingName], or the setting's default value if they chose
     * none; `null` if the setting is not registered.
     */
    suspend fun findSettingValueOrDefault(playerUuid: UUID, settingName: String): String? =
        suspendTransaction {
            SettingEntriesTable
                .select(SettingEntriesTable.value)
                .where((SettingEntriesTable.playerUuid eq playerUuid) and (SettingEntriesTable.settingName eq settingName))
                .map { it[SettingEntriesTable.value] }
                .firstOrNull()
                ?: SettingsTable
                    .select(SettingsTable.defaultValue)
                    .where(SettingsTable.name eq settingName)
                    .map { it[SettingsTable.defaultValue] }
                    .firstOrNull()
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
