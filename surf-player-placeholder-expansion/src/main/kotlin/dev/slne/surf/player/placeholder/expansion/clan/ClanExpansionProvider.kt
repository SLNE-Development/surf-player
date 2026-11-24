package dev.slne.surf.social.placeholder.expansion.clan

import dev.slne.surf.api.core.messages.adventure.uuid
import dev.slne.surf.social.api.clan.clan.Clan
import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.utils.Tags
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.tag.Tag

object ClanExpansionProvider {
    fun provideExpansion(): Expansion {
        Clan.registerListener(ClanDataCache)

        return Expansion.builder("clan")
            .author("Ammo, red & twisti")
            .version("1.2.0")
            .audiencePlaceholder("name") { audience, _, _ -> clanPlaceholder(audience) { text(it.name) } }
            .audiencePlaceholder("tag_raw") { audience, _, _ -> clanPlaceholder(audience) { text(it.tag) } }
            .audiencePlaceholder("tag") { audience, _, _ -> clanPlaceholder(audience) { it.renderedClanTag } }
            .build()
    }

    /** Inserts [render] of the audience's clan, or nothing when the audience is in no clan. */
    private fun clanPlaceholder(audience: Audience, render: (CachedClanData.Loaded) -> Component): Tag =
        when (val data = ClanDataCache.getData(audience.uuid())) {
            CachedClanData.Empty -> Tags.EMPTY_TAG
            is CachedClanData.Loaded -> Tag.inserting(render(data))
        }
}
