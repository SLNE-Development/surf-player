package dev.slne.surf.player.core.client.friends

import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.friends.redis.listener.FriendRequestRedisListener
import dev.slne.surf.player.core.client.friends.redis.listener.FriendshipRedisListener
import dev.slne.surf.player.core.client.friends.rpc.friendsRpcService
import dev.slne.surf.redis.sync.list.SyncList
import java.util.*

/**
 * The friends state shared by every client platform: all friendships and pending friend requests,
 * synchronized through the shared surf-player Redis connection owned by [SurfPlayerClient].
 *
 * The lifecycle is driven by the platform's friends feature.
 */
object FriendsClient {

    @Volatile
    lateinit var friendRequests: SyncList<FriendRequest>
        private set

    @Volatile
    lateinit var friendships: SyncList<Friendship>
        private set

    /**
     * Registers the friends Redis components. Must run before [SurfPlayerClient.connectRedis].
     */
    suspend fun load() {
        val redisApi = SurfPlayerClient.redisApi

        redisApi.subscribeToEvents(FriendshipRedisListener)
        redisApi.subscribeToEvents(FriendRequestRedisListener)

        friendRequests = redisApi.createSyncList<FriendRequest>("friend-requests")
        friendships = redisApi.createSyncList<Friendship>("friendships")
    }

    /**
     * Fetches the initial state. Requires the shared connections to be connected.
     */
    suspend fun enable() {
        fetch()
    }

    suspend fun fetch() {
        fetchFriendRequests()
        fetchFriendships()
    }

    suspend fun fetchFriendRequests() {
        val fetched = friendsRpcService.fetchFriendRequests()

        friendRequests.clear()
        fetched.forEach(friendRequests::add)
    }

    /** Removes the pending request from [senderUuid] to [targetUuid] from the synchronized list. */
    fun removeFriendRequest(senderUuid: UUID, targetUuid: UUID) {
        friendRequests.removeIf { it.isFrom(senderUuid, targetUuid) }
    }

    /** Removes the pending requests between [playerA] and [playerB] in both directions. */
    fun removeFriendRequestsBetween(playerA: UUID, playerB: UUID) {
        friendRequests.removeIf { it.isFrom(playerA, playerB) || it.isFrom(playerB, playerA) }
    }

    private fun FriendRequest.isFrom(sender: UUID, target: UUID) =
        senderUuid == sender && targetUuid == target

    suspend fun fetchFriendships() {
        val fetched = friendsRpcService.fetchFriendships()

        friendships.clear()
        fetched.forEach(friendships::add)
    }
}
