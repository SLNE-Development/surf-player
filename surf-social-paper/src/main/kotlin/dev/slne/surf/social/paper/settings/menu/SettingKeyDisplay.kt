package dev.slne.surf.social.paper.settings.menu

import org.bukkit.Material

data class SettingKeyDisplay(
    val displayName: String,
    val description: String,
    val material: Material,
    val category: SettingCategory,
)