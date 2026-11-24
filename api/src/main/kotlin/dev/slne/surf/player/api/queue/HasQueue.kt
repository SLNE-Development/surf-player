package dev.slne.surf.player.api.queue

import java.util.*

/** The server queues of a single player. See [SurfQueueApi]. */
interface HasQueue {
    val uuid: UUID
}
