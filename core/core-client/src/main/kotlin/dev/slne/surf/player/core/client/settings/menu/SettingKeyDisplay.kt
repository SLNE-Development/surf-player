package dev.slne.surf.player.core.client.settings.menu

import net.kyori.adventure.key.Key

/**
 * How a setting is shown in the settings menu.
 *
 * @property icon the key of the item type shown for this setting
 * @property options the values a player can pick, in the order a click cycles through them
 */
data class SettingKeyDisplay(
    val displayName: String,
    val description: String,
    val icon: Key,
    val category: SettingCategory,
    val options: List<SettingOption> = BOOLEAN_OPTIONS,
) {
    init {
        require(options.isNotEmpty()) { "Setting $displayName has no options" }
    }

    /** The option [value] stands for (ignoring case), or `null` if it is none of [options]. */
    fun optionOf(value: String): SettingOption? =
        options.firstOrNull { it.value.equals(value, ignoreCase = true) }

    /** The value after [value] in [options], wrapping around; the first option for unknown values. */
    fun nextValue(value: String): String {
        val index = options.indexOfFirst { it.value.equals(value, ignoreCase = true) }
        return options[(index + 1) % options.size].value
    }

    companion object {
        /** The options of a setting that is either on or off. */
        val BOOLEAN_OPTIONS = listOf(
            SettingOption("true", "Aktiviert"),
            SettingOption("false", "Deaktiviert"),
        )
    }
}

/**
 * One value a setting can take in the settings menu.
 *
 * @property value the stored value
 * @property label the name players see for it
 */
data class SettingOption(val value: String, val label: String)
