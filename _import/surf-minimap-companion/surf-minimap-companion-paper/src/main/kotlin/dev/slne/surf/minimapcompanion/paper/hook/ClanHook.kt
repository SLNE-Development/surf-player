package dev.slne.surf.minimapcompanion.paper.hook

import dev.slne.clan.api.clan.Clan
import dev.slne.surf.minimapcompanion.core.tracker.RelationSource
import java.util.*

/**
 * Relates players to the members of their clan from surf-clan. Only load it when surf-clan-paper is enabled.
 */
object ClanHook : RelationSource {
    override suspend fun related(player: UUID): Set<UUID> =
        Clan.byPlayer(player)?.members?.mapTo(HashSet()) { it.uuid }.orEmpty()
}
