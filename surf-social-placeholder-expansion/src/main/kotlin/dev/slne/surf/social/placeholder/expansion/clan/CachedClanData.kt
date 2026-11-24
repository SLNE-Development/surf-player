package dev.slne.surf.social.placeholder.expansion.clan

import net.kyori.adventure.text.Component

/** The clan data of one player as cached by [ClanDataCache]. */
sealed interface CachedClanData {
    /** The player is in no clan, or the data has not been loaded yet. */
    data object Empty : CachedClanData

    data class Loaded(
        val name: String,
        val tag: String,
        val renderedClanTag: Component
    ) : CachedClanData
}
