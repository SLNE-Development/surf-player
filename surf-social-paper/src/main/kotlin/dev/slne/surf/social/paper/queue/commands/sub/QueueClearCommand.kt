package dev.slne.surf.social.paper.queue.commands.sub

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.social.core.client.queue.command.QueueMessages
import dev.slne.surf.social.core.client.queue.queue.ClientQueue
import dev.slne.surf.social.core.common.queue.queue.RedisQueueService
import dev.slne.surf.social.paper.queue.permission.PaperQueuePermissions

fun CommandAPICommand.queueClear() = subcommand("clear") {
    withPermission(PaperQueuePermissions.COMMAND_CLEAR)
    surfBackendServerArgument("server", optional = true)

    anyExecutorSuspend { sender, arguments ->
        val server: SurfServer? by arguments
        val serverName = server?.name ?: SurfServer.current().name
        val queue = RedisQueueService.get().getQueueByName(serverName) as ClientQueue

        val size = queue.size()
        queue.delete()

        sender.sendMessage(QueueMessages.queueCleared(serverName, size))
    }
}
