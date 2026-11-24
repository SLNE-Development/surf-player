package dev.slne.surf.player.api.friends

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.player.api.friends.result.FriendRequestStateResult
import dev.slne.surf.player.api.friends.result.FriendshipRemoveResult
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

private val instance = requiredService<SurfFriendsApi>()

/**
 * Entry point to friendships and friend requests.
 *
 * Non-suspending members answer from the friendships and requests synchronized to this server.
 * Everything about a single player is also available through [FriendsCapability].
 */
interface SurfFriendsApi {

    /** The pending friend requests the player identified by [uuid] received. */
    fun receivedFriendRequests(uuid: UUID): @UnmodifiableView ObjectSet<FriendRequest>

    /** The pending friend requests the player identified by [uuid] sent. */
    fun sentFriendRequests(uuid: UUID): @UnmodifiableView ObjectSet<FriendRequest>

    /** The friendships of the player identified by [uuid], seen from their side. */
    fun friendships(uuid: UUID): @UnmodifiableView ObjectSet<Friendship>

    /** The friends of the player identified by [uuid] that are online anywhere in the network. */
    fun onlineFriendUuids(uuid: UUID): @UnmodifiableView ObjectSet<UUID>

    /** Sends a friend request from the player identified by [uuid] to [target]. */
    suspend fun sendFriendRequest(uuid: UUID, target: UUID): FriendRequestCreateResult

    /** Revokes the friend request the player identified by [uuid] sent to [target]. */
    suspend fun revokeFriendRequest(uuid: UUID, target: UUID): FriendRequestRemoveResult

    /** Accepts the friend request the player identified by [uuid] received from [sender]. */
    suspend fun acceptFriendRequest(uuid: UUID, sender: UUID): FriendRequestStateResult

    /** Declines the friend request the player identified by [uuid] received from [sender]. */
    suspend fun declineFriendRequest(uuid: UUID, sender: UUID): FriendRequestStateResult

    /** Whether the player identified by [uuid] sent a pending friend request to [target]. */
    fun hasSentFriendRequest(uuid: UUID, target: UUID): Boolean

    /** Whether the player identified by [uuid] received a pending friend request from [sender]. */
    fun hasReceivedFriendRequest(uuid: UUID, sender: UUID): Boolean

    /** The friendship of the player identified by [uuid] with [friend], or `null`. */
    fun findFriendship(uuid: UUID, friend: UUID): Friendship?

    /** Whether the player identified by [uuid] is friends with [friend]. */
    fun hasFriendship(uuid: UUID, friend: UUID): Boolean

    /** Ends the friendship of the player identified by [uuid] with [friend]. */
    suspend fun removeFriendship(uuid: UUID, friend: UUID): FriendshipRemoveResult

    /** Whether the player identified by [uuid] is notified when friends join or leave. */
    fun friendNotificationsEnabled(uuid: UUID): Boolean

    /** Whether the player identified by [uuid] hears a sound with friend notifications. */
    fun friendSoundsEnabled(uuid: UUID): Boolean

    /** Whether the player identified by [uuid] is notified about friend requests they receive. */
    fun friendRequestNotificationsEnabled(uuid: UUID): Boolean

    companion object : SurfFriendsApi by instance {
        val INSTANCE: SurfFriendsApi get() = instance
    }
}
