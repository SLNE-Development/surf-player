package dev.slne.surf.player.paper.minimap.hook

import dev.slne.surf.player.api.friends.player.FriendsPlayer
import dev.slne.surf.player.core.client.minimap.tracker.RelationSource
import java.util.*

/**
 * Relates players to their friends.
 */
object FriendsHook : RelationSource {
    override suspend fun related(player: UUID): Set<UUID> =
        FriendsPlayer[player].friendships.mapTo(HashSet()) { it.friendUuid }
}
