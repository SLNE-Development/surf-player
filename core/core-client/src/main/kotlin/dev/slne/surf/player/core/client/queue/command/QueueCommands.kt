package dev.slne.surf.player.core.client.queue.command

import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.failure
import dev.slne.surf.player.core.client.command.notice
import dev.slne.surf.player.core.client.command.reply
import dev.slne.surf.player.core.client.queue.config.SurfQueueConfig
import dev.slne.surf.player.core.client.queue.metrics.QueueMetrics
import dev.slne.surf.player.core.client.queue.metrics.QueueMetricsLogger
import dev.slne.surf.player.core.client.queue.queue.ClientQueue
import dev.slne.surf.player.core.client.util.usernameOrUuid
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService
import dev.slne.surf.api.core.util.mapAsync
import net.kyori.adventure.audience.Audience
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * The queue admin actions of Paper and Minestom (`/surfplayer queue ...`); the platforms only parse
 * the arguments. [serverName] is the queue of the backend server the action is about.
 */
object QueueCommands {

    private fun queue(serverName: String) = RedisQueueService.getQueueByName(serverName)

    fun reload(): CommandOutcome {
        val millis = measureTimeMillis { SurfQueueConfig.reload() }
        return reply(CommandMessages.configReloaded("queue", millis))
    }

    suspend fun list(serverName: String, page: Int): CommandOutcome {
        val entries = queue(serverName).getAllUuidsOrderedByPosition()
        if (entries.isEmpty()) return notice(QueueMessages.queueEmpty(serverName))

        val named = entries.mapAsync { it to usernameOrUuid(it) }
        return reply(QueueMessages.queueListPagination(serverName).renderComponent(named, page))
    }

    suspend fun info(player: UUID, playerName: String, serverName: String): CommandOutcome {
        val queue = queue(serverName)
        if (!queue.isQueued(player)) return failure(QueueMessages.playerNotQueued(playerName))

        return reply(
            QueueMessages.playerQueueInfo(
                playerName = playerName,
                uuid = player,
                position = queue.getPosition(player),
                size = queue.size(),
                meta = queue.getEntryMeta(player),
                score = queue.getEntryScore(player),
                lastSeen = queue.getEntryLastSeen(player),
                retryCount = queue.getEntryRetryCount(player)
            )
        )
    }

    suspend fun enqueue(player: UUID, playerName: String, serverName: String, priority: Int?): CommandOutcome {
        val queue = queue(serverName)
        val enqueued = priority?.let { queue.enqueue(player, it) } ?: queue.enqueue(player)

        if (!enqueued) return failure(QueueMessages.playerAlreadyQueued(playerName))
        return reply(QueueMessages.playerEnqueued(playerName, serverName, priority))
    }

    suspend fun dequeue(player: UUID, playerName: String, serverName: String): CommandOutcome {
        if (!queue(serverName).dequeue(player)) return failure(QueueMessages.playerNotQueued(playerName))
        return reply(QueueMessages.playerDequeued(playerName, serverName))
    }

    suspend fun clear(serverName: String): CommandOutcome {
        val queue = queue(serverName) as ClientQueue
        val size = queue.size()
        queue.clear()

        return reply(QueueMessages.queueCleared(serverName, size))
    }

    suspend fun cleanup(serverName: String): CommandOutcome {
        val queue = queue(serverName) as ClientQueue
        val sizeBefore = queue.size()
        queue.forceCleanup()

        return reply(QueueMessages.cleanupDone(serverName, sizeBefore, queue.size()))
    }

    suspend fun fix(serverName: String): CommandOutcome {
        val queue = queue(serverName) as ClientQueue
        return reply(QueueMessages.queueFixed(serverName, queue.fix()))
    }

    suspend fun pause(serverName: String): CommandOutcome {
        queue(serverName).pause()
        return reply(QueueMessages.queuePaused(serverName))
    }

    suspend fun resume(serverName: String): CommandOutcome {
        queue(serverName).resume()
        return reply(QueueMessages.queueResumed(serverName))
    }

    suspend fun status(serverName: String): CommandOutcome =
        reply(QueueMessages.queuePauseStatus(serverName, queue(serverName).isPaused()))

    fun startMetrics(): CommandOutcome {
        QueueMetricsLogger.stop()
        QueueMetricsLogger.start()
        return reply(QueueMessages.startedLoggingMetrics())
    }

    fun stopMetrics(): CommandOutcome {
        QueueMetricsLogger.stop()
        return reply(QueueMessages.stoppedLoggingMetrics())
    }

    suspend fun metricsSnapshot(audience: Audience) {
        val snapshot = QueueMetrics.snapshot()
        val queueSizes = QueueMetrics.collectQueueSizes()

        QueueMessages.metricsSnapshot(snapshot, queueSizes).forEach(audience::sendMessage)
    }
}
