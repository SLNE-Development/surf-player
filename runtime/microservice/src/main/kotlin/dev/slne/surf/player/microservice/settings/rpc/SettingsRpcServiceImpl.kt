package dev.slne.surf.player.microservice.settings.rpc

import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.common.settings.rpc.SettingsRpcService
import dev.slne.surf.player.microservice.settings.repository.PlayerSettingsRepository
import dev.slne.surf.player.microservice.settings.repository.SettingRepository
import java.util.*

object SettingsRpcServiceImpl : SettingsRpcService {
    override suspend fun findAllSettings(): List<Setting> = SettingRepository.loadSettings()

    override suspend fun createSettings(defaultValues: Map<String, String>): List<Setting> =
        SettingRepository.createSettings(defaultValues)

    override suspend fun findPlayerSettings(playerUuid: UUID): Map<String, String> =
        PlayerSettingsRepository.loadSettingsByPlayerUuid(playerUuid)

    override suspend fun savePlayerSetting(playerUuid: UUID, settingName: String, value: String) {
        PlayerSettingsRepository.savePlayerSetting(playerUuid, settingName, value)
    }
}
