package dev.slne.surf.player.velocity.queue

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.queue.QueueSystem
import dev.slne.surf.player.core.common.queue.queue.tick.QueueScheduler
import dev.slne.surf.player.velocity.VelocityPlayerFeature
import dev.slne.surf.player.velocity.plugin
import dev.slne.surf.player.velocity.queue.command.queueCommand
import dev.slne.surf.player.velocity.queue.config.VelocityQueueConfig
import dev.slne.surf.player.velocity.queue.listener.QueuePlayerListener
import dev.slne.surf.player.velocity.queue.listener.QueuePriorityListener
import dev.slne.surf.player.velocity.queue.queue.VelocityQueueImpl

/**
 * The proxy side of the server queue (formerly the `surf-queue-velocity` plugin): shows queued
 * players their position, keeps them queued across short disconnects and requeues them after a
 * reconnect.
 */
object QueueVelocityFeature : VelocityPlayerFeature {
    override val id = "queue"

    override suspend fun onLoad() {
        VelocityQueueConfig.init()

        val queueScheduler =
            QueueScheduler(workerCount = 2) // More queues, but the work is primarily only displaying the position in the action bar
        QueueSystem.init(
            redisApi = { SurfPlayerClient.redisApi },
            queueScheduler = queueScheduler,
            queueFactory = { serverName -> VelocityQueueImpl(serverName, queueScheduler) },
        )
    }

    override suspend fun onEnable() {
        QueueSystem.enable()
        QueueSystem.isLoaded = true

        plugin.eventManager.register(plugin, QueuePlayerListener)
        QueuePriorityListener.register()
        queueCommand()
    }

    override suspend fun onDisable() {
        QueuePriorityListener.unregister()
        QueueSystem.disable()
    }
}
