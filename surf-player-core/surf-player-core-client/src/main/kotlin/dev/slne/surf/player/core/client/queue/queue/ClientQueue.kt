package dev.slne.surf.social.core.client.queue.queue

import dev.slne.surf.social.api.queue.SurfQueue
import dev.slne.surf.social.core.common.queue.queue.RedisQueueLockResetResult

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
