package dev.slne.surf.player.microservice.friends.cooldown

/** What ended a friend request in a way that keeps its sender from sending the next one right away. */
enum class FriendRequestCooldownKind {
    /** The receiver declined the request. */
    DECLINE,

    /** The sender withdrew the request. */
    WITHDRAW
}
