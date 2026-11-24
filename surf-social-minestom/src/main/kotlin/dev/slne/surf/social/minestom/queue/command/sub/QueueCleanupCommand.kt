package dev.slne.surf.social.minestom.queue.command.sub

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfBackendServerArgument
import dev.slne.surf.social.core.client.queue.command.QueueMessages
import dev.slne.surf.social.core.client.queue.permission.QueuePermissions
import dev.slne.surf.social.core.client.queue.queue.ClientQueue
import dev.slne.surf.social.core.common.queue.queue.RedisQueueService

fun CommandAPICommand.queueCleanup(): CommandAPICommand = withSubcommand(
    subcommand("cleanup") {
        withPermission(QueuePermissions.COMMAND_CLEANUP)
        surfBackendServerArgument("server", optional = true)

        anyExecutorSuspend { sender, arguments ->
            val server: SurfServer? by arguments
            val serverName = server?.name ?: SurfServer.current().name
            val queue = RedisQueueService.get().getQueueByName(serverName) as ClientQueue

            val sizeBefore = queue.size()
            queue.forceCleanup()
            val sizeAfter = queue.size()

            sender.sendMessage(QueueMessages.cleanupDone(serverName, sizeBefore, sizeAfter))
        }
    }
)
