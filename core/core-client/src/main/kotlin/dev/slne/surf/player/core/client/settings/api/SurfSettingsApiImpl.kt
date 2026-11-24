package dev.slne.surf.player.core.client.settings.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.PlayerSetting
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.api.settings.setting.SettingKey
import dev.slne.surf.player.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.player.core.client.settings.service.SettingsService
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfSettingsApi::class)
class SurfSettingsApiImpl : SurfSettingsApi, Services.Fallback {
    override fun <T : Any> getSettingValue(playerUuid: UUID, key: SettingKey<T>): T {
        val value = SettingsService.getSettingValueOrDefault(playerUuid, key.name)
            ?: return key.defaultValue
        return key.deserialize(value)
    }

    override suspend fun <T : Any> getCachedValueOrLoad(playerUuid: UUID, key: SettingKey<T>): T {
        if (SettingsService.getSettingForPlayer(playerUuid, key.name) == null) {
            SettingsService.cachePlayerSettings(playerUuid)
        }

        return getSettingValue(playerUuid, key)
    }

    override suspend fun <T : Any> saveSetting(playerUuid: UUID, key: SettingKey<T>, value: T) {
        save(playerUuid, key.name, key.serialize(value))
    }

    private suspend fun save(playerUuid: UUID, settingName: String, value: String) {
        val setting = SettingsService.getSettingByName(settingName) ?: return
        SettingsService.cacheAndSavePlayerSetting(
            playerUuid,
            PlayerSetting(setting = setting, settingValue = value)
        )
    }

    override suspend fun <T : Any> createSetting(key: SettingKey<T>): Setting =
        SettingsService.createSetting(key.name, key.serialize(key.defaultValue))

    override fun <T : Any> getSetting(key: SettingKey<T>): Setting? =
        SettingsService.getSettingByName(key.name)

    override fun getSettings(): ObjectSet<Setting> = SettingsService.settings

    override fun openSettingsGui(playerUuid: UUID) {
        SettingsPlatform.openSettingsGui(playerUuid)
    }
}
