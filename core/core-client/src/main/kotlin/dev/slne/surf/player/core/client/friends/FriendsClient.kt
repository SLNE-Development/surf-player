package dev.slne.surf.player.core.client.friends

import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.friends.redis.listener.FriendsRedisListener
import dev.slne.surf.player.core.client.friends.rpc.friendsRpcService
import dev.slne.surf.player.core.common.playerRedisKey
import dev.slne.surf.redis.sync.list.SyncList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

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

    /** How long a server may hold the reload lock before Redis releases it on its own. */
    private const val RELOAD_LOCK_LEASE_SECONDS = 60L

    /**
     * Registers the friends Redis components. Must run before [SurfPlayerClient.connectRedis].
     */
    suspend fun load() {
        val redisApi = SurfPlayerClient.redisApi

        redisApi.subscribeToEvents(FriendsRedisListener)

        friendRequests = redisApi.createSyncList<FriendRequest>(playerRedisKey("friends", "requests"))
        friendships = redisApi.createSyncList<Friendship>(playerRedisKey("friends", "friendships"))
    }

    /**
     * Fetches the initial state. Requires the shared connections to be connected.
     */
    suspend fun enable() {
        fetch()
    }

    /**
     * Replaces the synchronized lists with the state stored by the microservice.
     *
     * Every server does this on startup. A network-wide lock keeps two servers that start at the
     * same time from interleaving their clear and add operations, which used to duplicate entries.
     */
    suspend fun fetch() {
        withReloadLock {
            replaceAll(friendRequests, friendsRpcService.findAllFriendRequests())
            replaceAll(friendships, friendsRpcService.findAllFriendships())
        }
    }

    private suspend fun <T : Any> replaceAll(list: SyncList<T>, elements: List<T>) {
        list.clearAndAwait()

        coroutineScope {
            elements.map { async { list.addAndAwait(it) } }.awaitAll()
        }
    }

    private suspend fun withReloadLock(block: suspend () -> Unit) {
        val lock = SurfPlayerClient.redisApi.redisson.getLock(playerRedisKey("friends", "reload-lock"))
        // Coroutines may resume on another thread, so the lock is owned by a random id instead.
        val owner = ThreadLocalRandom.current().nextLong()

        lock.lockAsync(RELOAD_LOCK_LEASE_SECONDS, TimeUnit.SECONDS, owner).await()
        try {
            block()
        } finally {
            lock.unlockAsync(owner).await()
        }
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
}
