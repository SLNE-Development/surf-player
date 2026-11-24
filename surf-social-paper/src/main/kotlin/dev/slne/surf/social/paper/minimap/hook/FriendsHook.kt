package dev.slne.surf.social.paper.minimap.hook

import dev.slne.surf.social.api.friends.player.FriendsPlayer
import dev.slne.surf.social.core.client.minimap.tracker.RelationSource
import java.util.*

/**
 * Relates players to their friends from surf-friends. Only load it when surf-friends-paper is enabled.
 */
object FriendsHook : RelationSource {
    override suspend fun related(player: UUID): Set<UUID> =
        FriendsPlayer[player].friendships.mapTo(HashSet()) { it.friendUuid }
}
