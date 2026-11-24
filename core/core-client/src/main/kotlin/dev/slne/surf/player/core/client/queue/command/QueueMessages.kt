package dev.slne.surf.player.core.client.queue.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.player.core.client.queue.metrics.QueueMetricsSnapshot
import dev.slne.surf.player.core.client.queue.queue.QueueFixResult
import dev.slne.surf.player.core.common.queue.queue.RedisQueueScore
import dev.slne.surf.player.core.common.queue.queue.entry.QueueEntry
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * Renders every message the queue commands send, so that all platforms share the exact same
 * wording and layout.
 */
object QueueMessages {

    fun queueListPagination(serverName: String) = Pagination<Pair<UUID, String>> {
        title { primary("Warteschlange $serverName".toSmallCaps(), TextDecoration.BOLD) }
        rowRenderer { (uuid, name), i ->
            listOf(buildText {
                variableKey(i + 1)
                spacer(" — ")
                variableValue(name)
                hoverEvent(buildText { spacer(uuid.toString()) })
            })
        }
    }

    fun cleanupDone(serverName: String, sizeBefore: Int, sizeAfter: Int) = buildText {
        appendSuccessPrefix()
        success("Die Warteschlange ")
        variableValue(serverName)
        success(" wurde bereinigt: ")
        variableValue("${sizeBefore - sizeAfter}")
        success(" abgelaufene Einträge entfernt (")
        variableValue("$sizeBefore")
        spacer(" → ")
        variableValue("$sizeAfter")
        success(").")
    }

    fun queueCleared(serverName: String, size: Int) = buildText {
        appendSuccessPrefix()
        success("Die Warteschlange ")
        variableValue(serverName)
        success(" wurde geleert: ")
        variableValue("$size")
        success(" Einträge entfernt.")
    }

    fun playerDequeued(playerName: String, serverName: String) = buildText {
        appendSuccessPrefix()
        variableValue(playerName)
        success(" wurde aus der Warteschlange ")
        variableValue(serverName)
        success(" entfernt.")
    }

    fun playerNotQueued(playerName: String) = "$playerName ist nicht in der Warteschlange."

    fun playerAlreadyQueued(playerName: String) = "$playerName ist bereits in der Warteschlange."

    fun playerEnqueued(playerName: String, serverName: String, priority: Int?) = buildText {
        appendSuccessPrefix()
        variableValue(playerName)
        success(" wurde in die Warteschlange ")
        variableValue(serverName)
        success(" eingereiht")
        priority?.let {
            success(" (Priorität ")
            variableValue(it)
            success(")")
        }
        success(".")
    }

    fun queueFixed(serverName: String, result: QueueFixResult) = buildText {
        val locks = result.lockReset

        appendSuccessPrefix()
        success("Die Warteschlange ")
        variableValue(serverName)
        success(" wurde repariert.")
        appendNewline {
            appendSuccessPrefix()
            variableKey("Einträge: ")
            variableValue("${result.sizeBefore}")
            spacer(" → ")
            variableValue("${result.sizeAfter}")
            spacer(" (")
            variableValue("${result.removedEntries}")
            spacer(" entfernt)")
        }
        appendNewline {
            appendSuccessPrefix()
            variableKey("War pausiert: ")
            variableValue(if (result.wasPaused) "ja" else "nein")
            spacer(" (läuft wieder)")
        }
        appendNewline {
            appendSuccessPrefix()
            variableKey("Transfer-Semaphore: ")
            variableValue("gelöscht=${locks.transferDeleted}, neu angelegt=${locks.transferInitialized}")
        }
        appendNewline {
            appendSuccessPrefix()
            variableKey("Cleanup-Semaphore: ")
            variableValue("gelöscht=${locks.cleanupDeleted}, neu angelegt=${locks.cleanupInitialized}")
        }
    }

    fun playerQueueInfo(
        playerName: String,
        uuid: UUID,
        position: Int?,
        size: Int,
        meta: QueueEntry?,
        score: RedisQueueScore?,
        lastSeen: Long?,
        retryCount: Int?
    ) = buildText {
        appendInfoPrefix()
        primary("Warteschlange: ".toSmallCaps(), TextDecoration.BOLD)
        variableValue(playerName)
        appendNewline {
            appendInfoPrefix()
            variableKey("UUID: ")
            variableValue("$uuid")
        }
        appendNewline {
            appendInfoPrefix()
            variableKey("Position: ")
            variableValue("${(position ?: -1) + 1}")
            spacer(" / ")
            variableValue("$size")
        }
        if (meta != null) {
            appendNewline {
                appendInfoPrefix()
                variableKey("Priorität: ")
                variableValue("${meta.priority}")
            }
            appendNewline {
                val addedAt = Instant.ofEpochMilli(meta.addedAt)
                val timeInQueue = Duration.between(addedAt, Instant.now()).toSeconds().seconds
                appendInfoPrefix()
                variableKey("Eingereiht: ")
                variableValue(addedAt.toString())
                spacer(" (vor ")
                variableValue(timeInQueue.toString())
                spacer(")")
            }
        }
        if (score != null) {
            appendNewline {
                appendInfoPrefix()
                variableKey("Score: ")
                variableValue(String.format("%.0f", score.packed))
                spacer(" (")
                variableKey("priority")
                spacer("=")
                variableValue(score.priority)
                spacer(", ")
                variableKey("deltaMs")
                spacer("=")
                variableValue(score.deltaMs)
                spacer(", ")
                variableKey("seq")
                spacer("=")
                variableValue(score.sequence)
                spacer(")")
            }
        }

        appendNewline {
            appendInfoPrefix()
            variableKey("Zuletzt gesehen: ")
            if (lastSeen != null) {
                val lastSeenAt = Instant.ofEpochMilli(lastSeen)
                val sinceLastSeen = Duration.between(lastSeenAt, Instant.now()).toSeconds().seconds
                variableValue(lastSeenAt.toString())
                spacer(" (vor ")
                variableValue(sinceLastSeen.toString())
                spacer(")")
            } else {
                variableValue("-")
                spacer(" (online oder keine Daten)")
            }
        }

        appendNewline {
            appendInfoPrefix()
            variableKey("Wiederholungen: ")
            variableValue("${retryCount ?: 0}")
        }
    }

    fun queueEmpty(serverName: String) = "Die Warteschlange $serverName ist leer."

    fun startedLoggingMetrics() = buildText {
        appendSuccessPrefix()
        success("Die Metriken werden nun protokolliert.")
    }

    fun stoppedLoggingMetrics() = buildText {
        appendSuccessPrefix()
        success("Die Metriken werden nicht mehr protokolliert.")
    }

    /**
     * Renders the metrics overview as one component per message, in send order.
     */
    fun metricsSnapshot(
        snapshot: QueueMetricsSnapshot,
        queueSizes: Map<String, Int>
    ): ObjectList<Component> {
        val lines = ObjectArrayList<Component>()

        lines += buildText {
            appendInfoPrefix()
            primary("Warteschlangen-Metriken".toSmallCaps(), TextDecoration.BOLD)
        }
        lines += buildText {
            appendInfoPrefix()
            info("Transfers: ")
            variableValue("${snapshot.totalTransfers}")
            info(" erfolgreich, ")
            variableValue("${snapshot.totalFailedTransfers}")
            info(" fehlgeschlagen (")
            variableValue(String.format("%.1f%%", snapshot.transferSuccessRate * 100))
            info(")")
        }
        lines += buildText {
            appendInfoPrefix()
            info("Eingereiht: ")
            variableValue("${snapshot.totalEnqueues}")
            info(" | Entfernt: ")
            variableValue("${snapshot.totalDequeues}")
        }
        lines += buildText {
            appendInfoPrefix()
            info("Kulanzzeit abgelaufen: ")
            variableValue("${snapshot.totalGraceExpiries}")
            info(" | Wiederholungen erschöpft: ")
            variableValue("${snapshot.totalRetryExhausted}")
        }
        lines += buildText {
            appendInfoPrefix()
            info("Lock: ")
            variableValue("${snapshot.totalLockAcquired}/${snapshot.totalLockAttempts}")
            info(" (")
            variableValue(String.format("%.1f%%", snapshot.lockAcquisitionRate * 100))
            info(")")
        }
        lines += buildText {
            appendInfoPrefix()
            info("Bereinigung: ")
            variableValue("${snapshot.totalCleanupCycles}")
            info(" Durchläufe, ")
            variableValue("${snapshot.totalCleanupRemovals}")
            info(" entfernt")
        }
        lines += buildText {
            appendInfoPrefix()
            info("Ticks: ")
            variableValue("${snapshot.totalTicks}")
        }

        if (queueSizes.isNotEmpty()) {
            lines += buildText {
                appendInfoPrefix()
                info("Pro Warteschlange:")
            }
            for ((name, size) in queueSizes) {
                val perQueue = snapshot.perQueue[name]
                lines += buildText {
                    appendInfoPrefix()
                    variableValue(name)
                    info(": Größe=")
                    variableValue("$size")
                    info(", Transfers=")
                    variableValue("${perQueue?.transfers ?: 0}")
                    info(", fehlgeschlagen=")
                    variableValue("${perQueue?.failedTransfers ?: 0}")
                    info(", übersprungen=")
                    variableValue("${perQueue?.skips ?: 0}")
                }
            }
        }

        return lines
    }

    fun queuePaused(serverName: String) = buildText {
        appendSuccessPrefix()
        success("Die Warteschlange ")
        variableValue(serverName)
        success(" wurde pausiert.")
    }

    fun queueResumed(serverName: String) = buildText {
        appendSuccessPrefix()
        success("Die Warteschlange ")
        variableValue(serverName)
        success(" läuft wieder.")
    }

    fun queuePauseStatus(serverName: String, paused: Boolean) = buildText {
        appendInfoPrefix()
        info("Die Warteschlange ")
        variableValue(serverName)
        info(if (paused) " ist pausiert." else " läuft.")
    }
}
