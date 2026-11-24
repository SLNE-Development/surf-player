package dev.slne.surf.social.paper.minimap.hook

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.core.client.minimap.tracker.RelationSource
import java.util.*

/**
 * Relates players to the members of their clan from surf-clan. Only load it when surf-clan-paper is enabled.
 */
object ClanHook : RelationSource {
    override suspend fun related(player: UUID): Set<UUID> =
        Clan.byPlayer(player)?.members?.mapTo(HashSet()) { it.uuid }.orEmpty()
}
