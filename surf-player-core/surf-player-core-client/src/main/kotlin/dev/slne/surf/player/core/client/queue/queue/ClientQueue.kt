package dev.slne.surf.player.core.client.queue.queue

import dev.slne.surf.player.api.queue.SurfQueue
import dev.slne.surf.player.core.common.queue.queue.RedisQueueLockResetResult

interface ClientQueue : SurfQueue {
    suspend fun delete()
    suspend fun fix(): QueueFixResult
    suspend fun forceCleanup()
}

data class QueueFixResult(
    val sizeBefore: Int,
    val sizeAfter: Int,
    val wasPaused: Boolean,
    val lockReset: RedisQueueLockResetResult
) {
    val removedEntries = sizeBefore - sizeAfter
}
