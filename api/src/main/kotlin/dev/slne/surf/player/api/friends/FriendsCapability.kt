package dev.slne.surf.player.api.friends

import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.player.api.friends.result.FriendRequestStateResult
import dev.slne.surf.player.api.friends.result.FriendshipRemoveResult
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

/** Friendships and friend requests of a single player. See [SurfFriendsApi]. */
interface FriendsCapability {
    val uuid: UUID

    /** The pending friend requests this player received. */
    val receivedFriendRequests: @UnmodifiableView ObjectSet<FriendRequest>
        get() = SurfFriendsApi.receivedFriendRequests(uuid)

    /** The pending friend requests this player sent. */
    val sentFriendRequests: @UnmodifiableView ObjectSet<FriendRequest>
        get() = SurfFriendsApi.sentFriendRequests(uuid)

    /** The friendships of this player, seen from their side. */
    val friendships: @UnmodifiableView ObjectSet<Friendship>
        get() = SurfFriendsApi.friendships(uuid)

    /** The friends of this player that are online anywhere in the network. */
    val onlineFriendUuids: @UnmodifiableView ObjectSet<UUID>
        get() = SurfFriendsApi.onlineFriendUuids(uuid)

    /** Whether this player is notified when friends join or leave. */
    val friendNotificationsEnabled: Boolean
        get() = SurfFriendsApi.friendNotificationsEnabled(uuid)

    /** Whether this player hears a sound with friend notifications. */
    val friendSoundsEnabled: Boolean
        get() = SurfFriendsApi.friendSoundsEnabled(uuid)

    /** Whether this player is notified about friend requests they receive. */
    val friendRequestNotificationsEnabled: Boolean
        get() = SurfFriendsApi.friendRequestNotificationsEnabled(uuid)

    /** Sends a friend request from this player to [target]. */
    suspend fun sendFriendRequest(target: UUID): FriendRequestCreateResult =
        SurfFriendsApi.sendFriendRequest(uuid, target)

    /** Revokes the friend request this player sent to [target]. */
    suspend fun revokeFriendRequest(target: UUID): FriendRequestRemoveResult =
        SurfFriendsApi.revokeFriendRequest(uuid, target)

    /** Accepts the friend request this player received from [sender]. */
    suspend fun acceptFriendRequest(sender: UUID): FriendRequestStateResult =
        SurfFriendsApi.acceptFriendRequest(uuid, sender)

    /** Declines the friend request this player received from [sender]. */
    suspend fun declineFriendRequest(sender: UUID): FriendRequestStateResult =
        SurfFriendsApi.declineFriendRequest(uuid, sender)

    /** Whether this player sent a pending friend request to [target]. */
    fun hasSentFriendRequest(target: UUID): Boolean =
        SurfFriendsApi.hasSentFriendRequest(uuid, target)

    /** Whether this player received a pending friend request from [sender]. */
    fun hasReceivedFriendRequest(sender: UUID): Boolean =
        SurfFriendsApi.hasReceivedFriendRequest(uuid, sender)

    /** The friendship of this player with [friend], or `null`. */
    fun findFriendship(friend: UUID): Friendship? = SurfFriendsApi.findFriendship(uuid, friend)

    /** Whether this player is friends with [friend]. */
    fun hasFriendship(friend: UUID): Boolean = SurfFriendsApi.hasFriendship(uuid, friend)

    /** Ends the friendship of this player with [friend]. */
    suspend fun removeFriendship(friend: UUID): FriendshipRemoveResult =
        SurfFriendsApi.removeFriendship(uuid, friend)
}
