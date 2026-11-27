package dev.slne.surf.player.core.client.settings.menu

import dev.slne.surf.player.api.settings.setting.PlayerSetting
import dev.slne.surf.player.core.client.command.runServiceAction
import net.kyori.adventure.audience.Audience
import dev.slne.surf.player.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.player.core.client.settings.service.SettingsService
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * The settings players changed in the settings menu. They are only stored once the player closes
 * the menu, so clicking through the options of a setting costs no request.
 */
object SettingsMenuDrafts {
    private val drafts = ConcurrentHashMap<UUID, ConcurrentHashMap<String, PlayerSetting>>()

    /**
     * The settings of [category] with the values of [playerUuid], in display order. Only settings
     * whose default value is one of their menu options are shown.
     */
    fun categorySettings(playerUuid: UUID, category: SettingCategory): List<PlayerSetting> =
        SettingsService.getLoadedSettingsWithDefaults(playerUuid)
            .filter { playerSetting ->
                val display = settingKeyMappings[playerSetting.setting.name]
                display?.category == category &&
                        display.optionOf(playerSetting.setting.defaultValue) != null
            }
            .sortedBy { settingKeyMappings.keys.indexOf(it.setting.name) }

    /**
     * The option [playerUuid] sees selected for [playerSetting], including changes not yet saved.
     * A stored value that is none of the options shows as the option of the default value.
     */
    fun currentOption(
        playerUuid: UUID,
        playerSetting: PlayerSetting,
        display: SettingKeyDisplay,
    ): SettingOption {
        val value = drafts[playerUuid]?.get(playerSetting.setting.name)?.settingValue
            ?: playerSetting.settingValue

        return display.optionOf(value)
            ?: display.optionOf(playerSetting.setting.defaultValue)
            ?: display.options.first()
    }

    /** Selects the option after the current one of [playerSetting] for [playerUuid]. */
    fun selectNext(playerUuid: UUID, playerSetting: PlayerSetting, display: SettingKeyDisplay) {
        val newValue = display.nextValue(currentOption(playerUuid, playerSetting, display).value)

        drafts.computeIfAbsent(playerUuid) { ConcurrentHashMap() }[playerSetting.setting.name] =
            PlayerSetting(playerSetting.setting, newValue)
    }

    /**
     * Saves and forgets the changes of [playerUuid]; called when they close the menu. [audience]
     * hears about it if the microservice cannot be reached.
     */
    fun save(playerUuid: UUID, audience: Audience) {
        val changedSettings = drafts.remove(playerUuid) ?: return

        SettingsPlatform.launchAsync {
            runServiceAction(audience) {
                for (setting in changedSettings.values) {
                    SettingsService.cacheAndSavePlayerSetting(playerUuid, setting)
                }
            }
        }
    }
}
