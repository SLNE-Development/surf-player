package dev.slne.surf.player.velocity.queue.command.pause

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.api.velocity.command.executors.anyExecutorSuspend
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.velocity.command.argument.surfBackendServerArgument
import dev.slne.surf.player.api.queue.queue
import dev.slne.surf.player.core.client.queue.command.QueueMessages
import dev.slne.surf.player.velocity.queue.permission.SurfQueuePermissions

fun CommandTree.queuePauseCommand() = literalArgument("pause") {
    withPermission(SurfQueuePermissions.COMMAND_PAUSE)

    surfBackendServerArgument("server") {
        literalArgument("pause") {
            anyExecutorSuspend { source, arguments ->
                val server: SurfServer by arguments
                server.queue().pause()

                source.sendMessage(QueueMessages.queuePaused())
            }
        }

        literalArgument("resume") {
            anyExecutorSuspend { source, arguments ->
                val server: SurfServer by arguments
                server.queue().resume()

                source.sendMessage(QueueMessages.queueResumed())
            }
        }

        literalArgument("status") {
            anyExecutorSuspend { source, arguments ->
                val server: SurfServer by arguments

                source.sendMessage(QueueMessages.queuePauseStatus(server.queue().isPaused()))
            }
        }
    }
}
