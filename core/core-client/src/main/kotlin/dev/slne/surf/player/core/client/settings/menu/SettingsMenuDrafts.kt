package dev.slne.surf.player.core.client.settings.menu

import dev.slne.surf.player.api.settings.setting.PlayerSetting
import dev.slne.surf.player.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.player.core.client.settings.service.SettingsService
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * The settings players toggled in the settings menu. They are only stored once the player closes
 * the menu, so toggling a setting back and forth costs no request.
 */
object SettingsMenuDrafts {
    private val drafts = ConcurrentHashMap<UUID, ConcurrentHashMap<String, PlayerSetting>>()

    /**
     * The boolean settings of [category] with the values of [playerUuid], in display order.
     */
    fun categorySettings(playerUuid: UUID, category: SettingCategory): List<PlayerSetting> =
        SettingsService.getLoadedSettingsWithDefaults(playerUuid)
            .filter { it.setting.defaultValue.toBooleanStrictOrNull() != null }
            .filter { settingKeyMappings[it.setting.name]?.category == category }
            .sortedBy { settingKeyMappings.keys.indexOf(it.setting.name) }

    /** The value [playerUuid] sees for [playerSetting], including changes not yet saved. */
    fun currentValue(playerUuid: UUID, playerSetting: PlayerSetting): Boolean =
        drafts[playerUuid]?.get(playerSetting.setting.name)?.getBoolean()
            ?: playerSetting.getBoolean()

    fun toggle(playerUuid: UUID, playerSetting: PlayerSetting) {
        val newValue = !currentValue(playerUuid, playerSetting)

        drafts.computeIfAbsent(playerUuid) { ConcurrentHashMap() }[playerSetting.setting.name] =
            PlayerSetting(playerSetting.setting, newValue.toString())
    }

    /** Saves and forgets the changes of [playerUuid]; called when they close the menu. */
    fun save(playerUuid: UUID) {
        val changedSettings = drafts.remove(playerUuid) ?: return

        SettingsPlatform.launchAsync {
            for (setting in changedSettings.values) {
                SettingsService.cacheAndSavePlayerSetting(playerUuid, setting)
            }
        }
    }
}
