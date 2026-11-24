package dev.slne.surf.player.core.client.settings.menu

import net.kyori.adventure.key.Key

/**
 * How a setting is shown in the settings menu.
 *
 * @property icon the key of the item type shown for this setting
 */
data class SettingKeyDisplay(
    val displayName: String,
    val description: String,
    val icon: Key,
    val category: SettingCategory,
)
