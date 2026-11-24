package dev.slne.surf.social.api.settings.setting

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSetting(
    val setting: Setting,
    var settingValue: String
) {
    fun getBoolean(): Boolean = settingValue.toBoolean()
    fun getInt(): Int = settingValue.toInt()
    fun getDouble(): Double = settingValue.toDouble()
    fun getString(): String = settingValue

    fun toggle() {
        if (setting.isBoolean()) {
            val newValue = (!getBoolean()).toString()
            settingValue = newValue
        }
    }
}
