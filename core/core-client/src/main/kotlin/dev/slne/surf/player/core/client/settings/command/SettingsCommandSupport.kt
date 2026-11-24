package dev.slne.surf.player.core.client.settings.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.api.settings.setting.PlayerSetting
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.player.core.client.settings.service.DeclaredSettings
import dev.slne.surf.player.core.client.settings.service.SettingsService
import dev.slne.surf.player.core.client.command.runServiceAction
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.util.*

object SettingsCommandActions {
    /** Toggles [setting] of [playerUuid]; [audience] hears about it if the save cannot reach the microservice. */
    fun toggle(playerUuid: UUID, audience: Audience, setting: Setting): String {
        val playerSetting = getPlayerSetting(playerUuid, setting).copy()
        playerSetting.toggle()
        cacheAndSave(playerUuid, audience, playerSetting)
        return playerSetting.getString()
    }

    /** Sets [setting] of [playerUuid]; [audience] hears about it if the save cannot reach the microservice. */
    fun set(playerUuid: UUID, audience: Audience, setting: Setting, state: Boolean): String {
        val playerSetting = getPlayerSetting(playerUuid, setting).copy()
        playerSetting.settingValue = state.toString()
        cacheAndSave(playerUuid, audience, playerSetting)
        return playerSetting.getString()
    }

    fun get(playerUuid: UUID, setting: Setting): String =
        getPlayerSetting(playerUuid, setting).getString()

    private fun getPlayerSetting(playerUuid: UUID, setting: Setting): PlayerSetting =
        SettingsService.getSettingForPlayerOrDefault(playerUuid, setting.name)
            ?: error("Setting not found")

    private fun cacheAndSave(playerUuid: UUID, audience: Audience, playerSetting: PlayerSetting) {
        SettingsService.cachePlayerSetting(playerUuid, playerSetting)
        SettingsPlatform.launchAsync {
            runServiceAction(audience) {
                SettingsService.savePlayerSetting(playerUuid, playerSetting)
            }
        }
    }
}

object SettingsCommandMessages {
    val booleanOnly: Component
        get() = buildText {
            appendErrorPrefix()
            error("Nur Boolean Einstellungen können über den Befehl geändert werden.")
        }

    val settingNotFound: Component
        get() = buildText {
            appendErrorPrefix()
            error("Die Einstellung wurde nicht gefunden.")
        }

    val noSettings: Component
        get() = buildText {
            appendInfoPrefix()
            info("Es sind keine Einstellungen verfügbar.")
        }

    fun changed(settingName: String, value: String): Component = buildText {
        appendSuccessPrefix()
        success("Die Einstellung ")
        variableValue(settingName)
        success(" wurde auf ")
        variableValue(value)
        success(" gesetzt.")
    }

    fun info(settingName: String, value: String): Component = buildText {
        appendInfoPrefix()
        info("Die Einstellung ")
        variableValue(settingName)
        info(" hat den Wert ")
        variableValue(value)
        info(".")
    }

    fun refreshed(settingCount: Int, elapsedMillis: Long): Component = buildText {
        appendSuccessPrefix()
        success("Es wurden alle Einstellungen neu geladen ($settingCount in ${elapsedMillis}ms)!")
    }

    /**
     * Every known setting: first those this server declares, then those only stored in the
     * database (created on other servers).
     */
    fun list(settings: DeclaredSettings): Component = buildText {
        appendInfoPrefix()
        info("Von diesem Server deklariert: ")
        variableValue(settings.declared.joinToString { it.name }.ifEmpty { "-" })
        appendNewline()
        appendInfoPrefix()
        info("Von anderen Servern: ")
        variableValue(settings.others.joinToString { it.name }.ifEmpty { "-" })
    }
}
