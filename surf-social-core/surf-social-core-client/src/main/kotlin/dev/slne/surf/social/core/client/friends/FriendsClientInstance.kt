package dev.slne.surf.social.core.client.friends

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.social.api.friends.model.FriendRequest
import dev.slne.surf.social.api.friends.model.Friendship
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.client.friends.redis.listener.FriendRequestRedisListener
import dev.slne.surf.social.core.client.friends.redis.listener.FriendshipRedisListener
import dev.slne.surf.social.core.common.friends.packets.friendrequest.FetchFriendRequestsRequestPacket
import dev.slne.surf.social.core.common.friends.packets.friendship.FetchFriendshipsRequestPacket
import dev.slne.surf.redis.sync.list.SyncList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

/**
 * The friends state shared by every client platform. It uses the shared surf-social connections
 * owned by [SocialClient].
 */
abstract class FriendsClientInstance {
    abstract val dataPath: Path

    @Volatile
    lateinit var friendRequests: SyncList<FriendRequest>

    @Volatile
    lateinit var friendships: SyncList<Friendship>

    private var subscribeToEvents: Boolean = false

    /**
     * Registers the friends Redis components. Must run before [SocialClient.connectRedis].
     */
    suspend fun onLoad() {
        val redisApi = SocialClient.redisApi

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

    /** The shared connections are closed by [SocialClient.disable]. */
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
        val fetched = SocialClient.rabbitApi.sendRequest(
            FetchFriendRequestsRequestPacket()
        ).friendRequests

        friendRequests.clear()
        fetched.forEach(friendRequests::add)
    }

    suspend fun fetchFriendships() {
        val fetched = SocialClient.rabbitApi.sendRequest(
            FetchFriendshipsRequestPacket()
        ).friendships

        friendships.clear()
        fetched.forEach(friendships::add)
    }

    companion object {
        val INSTANCE get() = instance
    }
}

private val instance = requiredService<FriendsClientInstance>()
