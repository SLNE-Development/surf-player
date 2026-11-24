package dev.slne.surf.social.placeholder.expansion.clan

import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.provider.ExpansionProvider
import io.github.miniplaceholders.api.provider.LoadRequirement

class PlaceholderExpansion : ExpansionProvider {
    override fun provideExpansion(): Expansion = ClanExpansionProvider.provideExpansion()
    override fun loadRequirement(): LoadRequirement =
        LoadRequirement.requiredComplement("surf-clan-velocity", "surf-clan-paper")
}