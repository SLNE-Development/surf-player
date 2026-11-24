package dev.slne.surf.player.core.client.friends

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.friends.redis.listener.FriendRequestRedisListener
import dev.slne.surf.player.core.client.friends.redis.listener.FriendshipRedisListener
import dev.slne.surf.player.core.client.friends.rpc.friendsRpcService
import dev.slne.surf.redis.sync.list.SyncList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import java.nio.file.Path
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * The friends state shared by every client platform. It uses the shared surf-player connections
 * owned by [SurfPlayerClient].
 */
abstract class FriendsClientInstance {
    abstract val dataPath: Path

    @Volatile
    lateinit var friendRequests: SyncList<FriendRequest>

    @Volatile
    lateinit var friendships: SyncList<Friendship>

    private var subscribeToEvents: Boolean = false

    /**
     * Registers the friends Redis components. Must run before [SurfPlayerClient.connectRedis].
     */
    suspend fun onLoad() {
        val redisApi = SurfPlayerClient.redisApi

        if (subscribeToEvents) {
            redisApi.subscribeToEvents(FriendshipRedisListener)
            redisApi.subscribeToEvents(FriendRequestRedisListener)
        }

        friendRequests = redisApi.createSyncList<FriendRequest>("friend-requests")
        friendships = redisApi.createSyncList<Friendship>("friendships")
    }

    /**
     * Fetches the initial state. Requires the shared connections to be connected.
     */
    suspend fun onEnable() {
        fetch()
    }

    /** The shared connections are closed by [SurfPlayerClient.disable]. */
    suspend fun onDisable() {
    }

    fun withEvents() {
        subscribeToEvents = true
    }

    abstract val mainDispatcher: CoroutineContext

    abstract fun launch(
        context: CoroutineContext = mainDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job

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

    companion object {
        val INSTANCE get() = instance
    }
}

private val instance = requiredService<FriendsClientInstance>()
