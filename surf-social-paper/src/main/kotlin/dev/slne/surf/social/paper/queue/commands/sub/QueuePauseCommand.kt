package dev.slne.surf.social.paper.queue.commands.sub

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.social.core.client.queue.command.QueueMessages
import dev.slne.surf.social.core.common.queue.queue.RedisQueueService
import dev.slne.surf.social.paper.queue.permission.PaperQueuePermissions

fun CommandAPICommand.queuePause() = subcommand("pause") {
    withPermission(PaperQueuePermissions.COMMAND_PAUSE)

    subcommand("pause") {
        surfBackendServerArgument("server", optional = true)
        anyExecutorSuspend { sender, arguments ->
            val server: SurfServer? by arguments
            val serverName = server?.name ?: SurfServer.current().name
            val queue = RedisQueueService.get().getQueueByName(serverName)
            queue.pause()

            sender.sendMessage(QueueMessages.queuePaused())
        }
    }

    subcommand("resume") {
        surfBackendServerArgument("server", optional = true)
        anyExecutorSuspend { sender, arguments ->
            val server: SurfServer? by arguments
            val serverName = server?.name ?: SurfServer.current().name
            val queue = RedisQueueService.get().getQueueByName(serverName)
            queue.resume()

            sender.sendMessage(QueueMessages.queueResumed())
        }
    }

    subcommand("status") {
        surfBackendServerArgument("server", optional = true)
        anyExecutorSuspend { sender, arguments ->
            val server: SurfServer? by arguments
            val serverName = server?.name ?: SurfServer.current().name
            val queue = RedisQueueService.get().getQueueByName(serverName)

            sender.sendMessage(QueueMessages.queuePauseStatus(queue.isPaused()))
        }
    }
}
