package dev.slne.surf.player.minestom.queue.command.sub

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfBackendServerArgument
import dev.slne.surf.player.core.client.queue.command.QueueMessages
import dev.slne.surf.player.core.client.queue.permission.QueuePermissions
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService

fun CommandAPICommand.queuePause(): CommandAPICommand = withSubcommand(
    subcommand("pause") {
        withPermission(QueuePermissions.COMMAND_PAUSE)

        withSubcommand(subcommand("pause") {
            surfBackendServerArgument("server", optional = true)
            anyExecutorSuspend { sender, arguments ->
                val server: SurfServer? by arguments
                val serverName = server?.name ?: SurfServer.current().name
                val queue = RedisQueueService.getQueueByName(serverName)
                queue.pause()

                sender.sendMessage(QueueMessages.queuePaused())
            }
        })

        withSubcommand(subcommand("resume") {
            surfBackendServerArgument("server", optional = true)
            anyExecutorSuspend { sender, arguments ->
                val server: SurfServer? by arguments
                val serverName = server?.name ?: SurfServer.current().name
                val queue = RedisQueueService.getQueueByName(serverName)
                queue.resume()

                sender.sendMessage(QueueMessages.queueResumed())
            }
        })

        withSubcommand(subcommand("status") {
            surfBackendServerArgument("server", optional = true)
            anyExecutorSuspend { sender, arguments ->
                val server: SurfServer? by arguments
                val serverName = server?.name ?: SurfServer.current().name
                val queue = RedisQueueService.getQueueByName(serverName)

                sender.sendMessage(QueueMessages.queuePauseStatus(queue.isPaused()))
            }
        })
    }
)
