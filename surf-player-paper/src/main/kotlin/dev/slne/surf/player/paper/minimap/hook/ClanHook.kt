package dev.slne.surf.player.paper.minimap.hook

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.core.client.minimap.tracker.RelationSource
import java.util.*

/**
 * Relates players to the members of their clan.
 */
object ClanHook : RelationSource {
    override suspend fun related(player: UUID): Set<UUID> =
        Clan.byPlayer(player)?.members?.mapTo(HashSet()) { it.uuid }.orEmpty()
}
