package dev.slne.surf.player.api.settings

import dev.slne.surf.player.api.settings.setting.SettingKey
import java.util.*

/** The settings of a single player. See [SurfSettingsApi]. */
interface SettingsCapability {
    val uuid: UUID

    /**
     * The value of [key] for this player, answered from the settings cached on this server.
     * Returns the [SettingKey.defaultValue] if the player has no value set.
     */
    fun <T : Any> setting(key: SettingKey<T>): T = SurfSettingsApi.getSettingValue(uuid, key)

    /**
     * The value of [key] for this player, loading their settings first if they are not cached on
     * this server (e.g. because the player is offline).
     */
    suspend fun <T : Any> loadSetting(key: SettingKey<T>): T =
        SurfSettingsApi.getCachedValueOrLoad(uuid, key)

    /** Saves [value] as this player's value of [key]. */
    suspend fun <T : Any> saveSetting(key: SettingKey<T>, value: T) =
        SurfSettingsApi.saveSetting(uuid, key, value)

    /** Opens the settings menu for this player, if they are online here. */
    fun openSettingsMenu() = SurfSettingsApi.openSettingsGui(uuid)
}
