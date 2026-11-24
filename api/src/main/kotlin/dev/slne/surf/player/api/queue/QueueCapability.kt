package dev.slne.surf.player.api.queue

import java.util.*

/** The server queues of a single player. See [SurfQueueApi]. */
interface QueueCapability {
    val uuid: UUID

    /**
     * Enqueues this player into the queue of the server named [serverName], using the priority
     * resolved from LuckPerms.
     *
     * @return `true` if the player was newly added, `false` if already queued.
     */
    suspend fun enqueue(serverName: String): Boolean = SurfQueueApi.queue(serverName).enqueue(uuid)

    /**
     * Enqueues this player into the queue of the server named [serverName] with an explicit
     * [priority].
     *
     * @return `true` if the player was newly added, `false` if already queued.
     */
    suspend fun enqueue(serverName: String, priority: Int): Boolean =
        SurfQueueApi.queue(serverName).enqueue(uuid, priority)

    /**
     * Removes this player from the queue of the server named [serverName].
     *
     * @return `true` if the player was removed, `false` if they were not queued.
     */
    suspend fun dequeue(serverName: String): Boolean = SurfQueueApi.queue(serverName).dequeue(uuid)

    /** Whether this player is waiting in the queue of the server named [serverName]. */
    suspend fun isQueued(serverName: String): Boolean = SurfQueueApi.queue(serverName).isQueued(uuid)

    /**
     * The 0-based position of this player in the queue of the server named [serverName], or `null`
     * if they are not queued there.
     */
    suspend fun queuePosition(serverName: String): Int? =
        SurfQueueApi.queue(serverName).getPosition(uuid)
}
