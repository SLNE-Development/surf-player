package dev.slne.surf.social.minestom.queue.command.sub

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.client.queue.command.QueueMessages
import dev.slne.surf.social.core.client.queue.metrics.QueueMetrics
import dev.slne.surf.social.core.client.queue.metrics.QueueMetricsLogger
import dev.slne.surf.social.core.client.queue.permission.QueuePermissions

fun CommandAPICommand.metricsCommand(): CommandAPICommand = withSubcommand(
    subcommand("metrics") {
        withPermission(QueuePermissions.COMMAND_METRICS)

        withSubcommand(subcommand("startLogging") {
            anyExecutor { source, arguments ->
                QueueMetricsLogger.stop()
                QueueMetricsLogger.start()
                source.sendMessage(QueueMessages.startedLoggingMetrics())
            }
        })

        withSubcommand(subcommand("stopLogging") {
            anyExecutor { source, arguments ->
                QueueMetricsLogger.stop()
                source.sendMessage(QueueMessages.stoppedLoggingMetrics())
            }
        })

        withSubcommand(subcommand("snapshot") {
            anyExecutorSuspend { sender, _ ->
                val snapshot = QueueMetrics.snapshot()
                val queueSizes = QueueMetrics.collectQueueSizes()

                QueueMessages.metricsSnapshot(snapshot, queueSizes).forEach(sender::sendMessage)
            }
        })
    }
)
