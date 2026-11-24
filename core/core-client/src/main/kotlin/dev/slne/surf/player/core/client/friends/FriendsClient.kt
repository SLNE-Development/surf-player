package dev.slne.surf.player.core.client.friends

import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.friends.connection.FriendConnectionNotifier
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
        redisApi.subscribeToEvents(FriendConnectionNotifier)

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

    /**
     * Adds [request] and waits until Redis has it, so a server that reacts to the event published
     * afterwards can already read it there (see [hasFriendRequestCommitted]).
     */
    suspend fun addFriendRequest(request: FriendRequest) {
        friendRequests.addAndAwait(request)
    }

    /** Adds [friendship] and waits until Redis has it, see [addFriendRequest]. */
    suspend fun addFriendship(friendship: Friendship) {
        friendships.addAndAwait(friendship)
    }

    /**
     * Whether the request from [senderUuid] to [targetUuid] is pending. The local view of the
     * synchronized list may lag behind a request that another server just created (the event about
     * it can arrive first), so a miss is checked against Redis, which also refreshes the local view.
     */
    suspend fun hasFriendRequestCommitted(senderUuid: UUID, targetUuid: UUID): Boolean =
        presentLocallyOrRemotely(friendRequests) { it.isFrom(senderUuid, targetUuid) }

    /** Whether [playerUuid] is friends with [friendUuid], checked like [hasFriendRequestCommitted]. */
    suspend fun hasFriendshipCommitted(playerUuid: UUID, friendUuid: UUID): Boolean =
        presentLocallyOrRemotely(friendships) { it.playerUuid == playerUuid && it.friendUuid == friendUuid }

    private suspend fun <T : Any> presentLocallyOrRemotely(list: SyncList<T>, predicate: (T) -> Boolean): Boolean =
        list.snapshot().any(predicate) || list.snapshotRemote().any(predicate)

    /** Removes the pending request from [senderUuid] to [targetUuid] from the synchronized list. */
    suspend fun removeFriendRequest(senderUuid: UUID, targetUuid: UUID) {
        friendRequests.removeIfAndAwait { it.isFrom(senderUuid, targetUuid) }
    }

    /** Removes the pending requests between [playerA] and [playerB] in both directions. */
    fun removeFriendRequestsBetween(playerA: UUID, playerB: UUID) {
        friendRequests.removeIf { it.isFrom(playerA, playerB) || it.isFrom(playerB, playerA) }
    }

    /** Removes the friendship between [playerA] and [playerB] in both directions. */
    suspend fun removeFriendshipsBetween(playerA: UUID, playerB: UUID) {
        friendships.removeIfAndAwait {
            (it.playerUuid == playerA && it.friendUuid == playerB) ||
                    (it.playerUuid == playerB && it.friendUuid == playerA)
        }
    }

    private fun FriendRequest.isFrom(sender: UUID, target: UUID) =
        senderUuid == sender && targetUuid == target
}
