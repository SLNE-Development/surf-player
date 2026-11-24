package dev.slne.surf.player.core.client.settings

import dev.slne.surf.player.api.settings.setting.SettingKey
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.settings.service.SettingsService

/** The settings every platform registers on startup. */
private val builtinSettings: List<SettingKey<*>> = listOf(
    SettingKeys.CHAT_PINGS,
    SettingKeys.CHAT_DEATH_MESSAGES,
    SettingKeys.DIRECT_MESSAGES,
    SettingKeys.CONNECTION_MESSAGES,
    SettingKeys.LOBBY_SCROLL_SOUND,
    SettingKeys.CLAN_INVITES,
    SettingKeys.CLAN_CHAT_MESSAGES,
    SettingKeys.FRIEND_REQUESTS,
    SettingKeys.FRIEND_REQUEST_NOTIFICATIONS,
    SettingKeys.FRIEND_NOTIFICATIONS,
    SettingKeys.FRIEND_SOUNDS,
    SettingKeys.SHOW_NAMETAGS,
    SettingKeys.AUTO_QUEUE_AFTER_RECONNECT,
)

/**
 * Loads the registered settings and registers the built-in ones that are still missing, with one
 * call to the microservice. Every platform runs this once on startup, before players can join.
 */
suspend fun loadSettings() {
    SettingsService.refreshSettings()
    SettingsService.createSettings(builtinSettings.associate { it.name to it.serializedDefaultValue() })
}

private fun <T : Any> SettingKey<T>.serializedDefaultValue(): String = serialize(defaultValue)
