package dev.slne.surf.minimapcompanion.paper.hook

import dev.slne.surf.friends.api.player.FriendsPlayer
import dev.slne.surf.minimapcompanion.core.tracker.RelationSource
import java.util.*

/**
 * Relates players to their friends from surf-friends. Only load it when surf-friends-paper is enabled.
 */
object FriendsHook : RelationSource {
    override suspend fun related(player: UUID): Set<UUID> =
        FriendsPlayer[player].friendships.mapTo(HashSet()) { it.friendUuid }
}
