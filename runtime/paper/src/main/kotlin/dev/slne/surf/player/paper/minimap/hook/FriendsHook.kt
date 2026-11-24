package dev.slne.surf.player.paper.minimap.hook

import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.core.client.minimap.tracker.RelationSource
import java.util.*

/**
 * Relates players to their friends.
 */
object FriendsHook : RelationSource {
    override suspend fun related(player: UUID): Set<UUID> =
        SurfFriendsApi.friendships(player).mapTo(HashSet()) { it.friendUuid }
}
