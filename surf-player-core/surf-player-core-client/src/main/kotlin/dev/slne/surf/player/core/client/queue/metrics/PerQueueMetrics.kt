package dev.slne.surf.player.core.client.queue.metrics

/**
 * The counters of a single queue within a [QueueMetricsSnapshot].
 */
data class PerQueueMetrics(
    val transfers: Long,
    val enqueues: Long,
    val dequeues: Long,
    val failedTransfers: Long,
    val skips: Long
)
