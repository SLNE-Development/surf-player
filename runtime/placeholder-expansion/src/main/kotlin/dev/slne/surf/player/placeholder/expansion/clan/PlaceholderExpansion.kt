package dev.slne.surf.player.placeholder.expansion.clan

import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.provider.ExpansionProvider
import io.github.miniplaceholders.api.provider.LoadRequirement

/**
 * Loads the clan expansion from MiniPlaceholders' expansion folder on Velocity. Paper does not need
 * the jar: surf-player-paper bundles this module and registers the expansion itself
 * (`PaperClanPlaceholders`), so it neither depends on MiniPlaceholders-Paper's expansion class loader
 * seeing the plugin's classes nor on a jar being deployed. Requiring only the Velocity plugin keeps a
 * jar dropped into a Paper server's expansion folder from registering the expansion a second time.
 */
class PlaceholderExpansion : ExpansionProvider {
    override fun provideExpansion(): Expansion = ClanExpansionProvider.provideExpansion()
    override fun loadRequirement(): LoadRequirement =
        LoadRequirement.requiredComplement("surf-player-velocity")
}
