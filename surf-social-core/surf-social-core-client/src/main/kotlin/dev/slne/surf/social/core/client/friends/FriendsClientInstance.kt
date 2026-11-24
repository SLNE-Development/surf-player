package dev.slne.surf.social.core.client.friends

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.social.api.friends.model.FriendRequest
import dev.slne.surf.social.api.friends.model.Friendship
import dev.slne.surf.social.core.client.friends.redis.listener.FriendRequestRedisListener
import dev.slne.surf.social.core.client.friends.redis.listener.FriendshipRedisListener
import dev.slne.surf.social.core.common.friends.packets.friendrequest.FetchFriendRequestsRequestPacket
import dev.slne.surf.social.core.common.friends.packets.friendship.FetchFriendshipsRequestPacket
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.sync.list.SyncList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

abstract class FriendsClientInstance {
    abstract val dataPath: Path

    val redisApi = RedisApi.create("surf-friends")
    val rabbitApi = ClientRabbitMQApi.create("surf-friends", dataPath)

    @Volatile
    lateinit var friendRequests: SyncList<FriendRequest>

    @Volatile
    lateinit var friendships: SyncList<Friendship>

    private var subscribeToEvents: Boolean = false

    suspend fun onLoad() {
        rabbitApi.freezeAndConnect()

        if (subscribeToEvents) {
            redisApi.subscribeToEvents(FriendshipRedisListener)
            redisApi.subscribeToEvents(FriendRequestRedisListener)
        }

        friendRequests = redisApi.createSyncList<FriendRequest>("friend-requests")
        friendships = redisApi.createSyncList<Friendship>("friendships")
        redisApi.freezeAndConnect()
    }

    suspend fun onEnable() {
        fetch()
    }

    suspend fun onDisable() {
        redisApi.disconnect()
        rabbitApi.disconnect()
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
        val fetched = rabbitApi.sendRequest(
            FetchFriendRequestsRequestPacket()
        ).friendRequests

        friendRequests.clear()
        fetched.forEach(friendRequests::add)
    }

    suspend fun fetchFriendships() {
        val fetched = rabbitApi.sendRequest(
            FetchFriendshipsRequestPacket()
        ).friendships

        friendships.clear()
        fetched.forEach(friendships::add)
    }

    companion object {
        val INSTANCE get() = instance
    }
}

fun launch(
    context: CoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = instance.launch(context, start, block)

private val instance = requiredService<FriendsClientInstance>()
val redisApi get() = instance.redisApi
val rabbitApi get() = instance.rabbitApi