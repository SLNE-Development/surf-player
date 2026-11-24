package dev.slne.surf.player.paper.queue.commands.sub

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.player.core.client.queue.command.QueueMessages
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService
import dev.slne.surf.player.paper.queue.permission.PaperQueuePermissions

fun CommandAPICommand.queueList() = subcommand("list") {
    withPermission(PaperQueuePermissions.COMMAND_LIST)
    surfBackendServerArgument("server", optional = true)

    anyExecutorSuspend { sender, arguments ->
        val server: SurfServer? by arguments
        val serverName = server?.name ?: SurfServer.current().name
        val queue = RedisQueueService.getQueueByName(serverName)
        val entries = queue.getAllUuidsOrderedByPosition()

        if (entries.isEmpty()) {
            sender.sendMessage(QueueMessages.queueEmpty())
            return@anyExecutorSuspend
        }

        sender.sendMessage(QueueMessages.queueListPagination.renderComponent(entries))
    }
}
