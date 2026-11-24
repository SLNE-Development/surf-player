package dev.slne.surf.player.paper.queue.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.player.paper.queue.commands.sub.metricsCommand
import dev.slne.surf.player.paper.queue.commands.sub.queueDequeue
import dev.slne.surf.player.paper.queue.commands.sub.queueCleanup
import dev.slne.surf.player.paper.queue.commands.sub.queueClear
import dev.slne.surf.player.paper.queue.commands.sub.queueEnqueue
import dev.slne.surf.player.paper.queue.commands.sub.queueFix
import dev.slne.surf.player.paper.queue.commands.sub.queueInfo
import dev.slne.surf.player.paper.queue.commands.sub.queueList
import dev.slne.surf.player.paper.queue.commands.sub.queuePause
import dev.slne.surf.player.paper.queue.permission.PaperQueuePermissions

fun queueCommand() = commandAPICommand("squeue") {
    withPermission(PaperQueuePermissions.COMMAND_QUEUE)

    queueCleanup()
    queueClear()
    queueDequeue()
    queueEnqueue()
    queueFix()
    queueInfo()
    queueList()
    queuePause()
    metricsCommand()
}
