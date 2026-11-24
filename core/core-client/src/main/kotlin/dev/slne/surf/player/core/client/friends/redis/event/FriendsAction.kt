package dev.slne.surf.player.core.client.friends.redis.event

import kotlinx.serialization.Serializable

/** What a [FriendsRedisEvent] reports; the executor is always the player who acted. */
@Serializable
enum class FriendsAction {
    /** The executor sent a friend request to the target. */
    REQUEST_SENT,

    /** The executor withdrew their friend request to the target. */
    REQUEST_REVOKED,

    /** The executor accepted the friend request the target sent them. */
    REQUEST_ACCEPTED,

    /** The executor declined the friend request the target sent them. */
    REQUEST_DECLINED,

    /** The executor ended their friendship with the target. */
    FRIENDSHIP_REMOVED
}
