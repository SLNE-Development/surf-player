package dev.slne.surf.social.minestom.queue.command

import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.social.core.client.queue.permission.QueuePermissions
import dev.slne.surf.social.minestom.queue.command.sub.metricsCommand
import dev.slne.surf.social.minestom.queue.command.sub.queueCleanup
import dev.slne.surf.social.minestom.queue.command.sub.queueClear
import dev.slne.surf.social.minestom.queue.command.sub.queueDequeue
import dev.slne.surf.social.minestom.queue.command.sub.queueEnqueue
import dev.slne.surf.social.minestom.queue.command.sub.queueFix
import dev.slne.surf.social.minestom.queue.command.sub.queueInfo
import dev.slne.surf.social.minestom.queue.command.sub.queueList
import dev.slne.surf.social.minestom.queue.command.sub.queuePause

fun queueCommand() = commandAPICommand("squeue") {
    withPermission(QueuePermissions.COMMAND_QUEUE)

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
