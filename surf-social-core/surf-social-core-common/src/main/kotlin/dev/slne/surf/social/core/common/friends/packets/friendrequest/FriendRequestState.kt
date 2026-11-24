package dev.slne.surf.social.core.common.friends.packets.friendrequest

import kotlinx.serialization.Serializable

/**
 * How a friend request is answered.
 */
@Serializable
enum class FriendRequestState {
    ACCEPT,
    DECLINE
}
