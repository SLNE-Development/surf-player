package dev.slne.surf.social.velocity.queue

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.queue.QueueInstance
import dev.slne.surf.social.core.common.queue.queue.AbstractQueue
import dev.slne.surf.social.core.common.queue.queue.tick.QueueScheduler
import dev.slne.surf.social.velocity.queue.command.queueCommand
import dev.slne.surf.social.velocity.queue.config.VelocityQueueConfig
import dev.slne.surf.social.velocity.queue.listener.QueuePlayerListener
import dev.slne.surf.social.velocity.queue.listener.QueuePriorityListener
import dev.slne.surf.social.velocity.queue.queue.VelocityQueueImpl

@AutoService(QueueInstance::class)
class VelocitySurfQueueInstance : QueueInstance() {
    override val componentOwner get() = plugin.container
    override val queueScheduler: QueueScheduler =
        QueueScheduler(workerCount = 2) // More queues, but the work is primarily only displaying the position in the action bar

    @Volatile
    override var isLoaded: Boolean = false
        private set

    override suspend fun load() {
        VelocityQueueConfig.init()
        super.load()
    }

    override suspend fun enable() {
        super.enable()

        plugin.proxy.eventManager.register(plugin, QueuePlayerListener)
        QueuePriorityListener.register()
        queueCommand()
        isLoaded = true
    }

    override suspend fun disable() {
        QueuePriorityListener.unregister()
        super.disable()
    }

    override fun createQueue(serverName: String): AbstractQueue {
        return VelocityQueueImpl(serverName, queueScheduler)
    }
}