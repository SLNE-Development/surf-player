package dev.slne.surf.player.velocity.queue

import dev.slne.surf.player.core.common.queue.QueueInstance
import dev.slne.surf.player.velocity.VelocityPlayerFeature
import dev.slne.surf.player.velocity.plugin
import dev.slne.surf.player.velocity.queue.command.queueCommand
import dev.slne.surf.player.velocity.queue.config.VelocityQueueConfig
import dev.slne.surf.player.velocity.queue.listener.QueuePlayerListener
import dev.slne.surf.player.velocity.queue.listener.QueuePriorityListener

/**
 * The proxy side of the server queue (formerly the `surf-queue-velocity` plugin): shows queued
 * players their position, keeps them queued across short disconnects and requeues them after a
 * reconnect.
 */
object QueueVelocityFeature : VelocityPlayerFeature {
    override val id = "queue"

    override suspend fun onLoad() {
        VelocityQueueConfig.init()
    }

    override suspend fun onEnable() {
        QueueInstance.get().enable()

        plugin.eventManager.register(plugin, QueuePlayerListener)
        QueuePriorityListener.register()
        queueCommand()
    }

    override suspend fun onDisable() {
        QueuePriorityListener.unregister()
        QueueInstance.get().disable()
    }
}
