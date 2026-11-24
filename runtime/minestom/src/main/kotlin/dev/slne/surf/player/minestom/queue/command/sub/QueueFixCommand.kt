package dev.slne.surf.player.minestom.queue.command.sub

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfBackendServerArgument
import dev.slne.surf.player.core.client.queue.command.QueueMessages
import dev.slne.surf.player.core.client.queue.permission.QueuePermissions
import dev.slne.surf.player.core.client.queue.queue.ClientQueue
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService

fun CommandAPICommand.queueFix(): CommandAPICommand = withSubcommand(
    subcommand("fix") {
        withPermission(QueuePermissions.COMMAND_FIX)
        surfBackendServerArgument("server", optional = true)

        anyExecutorSuspend { sender, arguments ->
            val server: SurfServer? by arguments
            val serverName = server?.name ?: SurfServer.current().name
            val queue = RedisQueueService.getQueueByName(serverName) as ClientQueue
            val result = queue.fix()

            sender.sendMessage(QueueMessages.queueFixed(serverName, result))
        }
    }
)
