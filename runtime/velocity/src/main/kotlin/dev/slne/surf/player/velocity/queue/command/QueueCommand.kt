package dev.slne.surf.player.velocity.queue.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.player.velocity.queue.command.pause.queuePauseCommand
import dev.slne.surf.player.velocity.queue.permission.SurfQueuePermissions

fun queueCommand() = commandTree("squeue-velocity") {
    withPermission(SurfQueuePermissions.COMMAND_QUEUE)
    queuePauseCommand()
}