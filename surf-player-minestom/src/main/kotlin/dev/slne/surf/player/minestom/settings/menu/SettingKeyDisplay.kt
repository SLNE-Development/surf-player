package dev.slne.surf.player.minestom.settings.menu

import net.minestom.server.item.Material

data class SettingKeyDisplay(
    val displayName: String,
    val description: String,
    val material: Material,
    val category: SettingCategory,
)