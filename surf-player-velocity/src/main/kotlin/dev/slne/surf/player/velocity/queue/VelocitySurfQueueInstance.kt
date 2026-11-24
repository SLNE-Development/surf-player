package dev.slne.surf.player.velocity.queue

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.queue.QueueInstance
import dev.slne.surf.player.core.common.queue.queue.AbstractQueue
import dev.slne.surf.player.core.common.queue.queue.tick.QueueScheduler
import dev.slne.surf.player.velocity.queue.queue.VelocityQueueImpl

@AutoService(QueueInstance::class)
class VelocitySurfQueueInstance : QueueInstance() {
    override val redisApi get() = SurfPlayerClient.redisApi

    override val queueScheduler: QueueScheduler =
        QueueScheduler(workerCount = 2) // More queues, but the work is primarily only displaying the position in the action bar

    @Volatile
    override var isLoaded: Boolean = false
        private set

    override suspend fun enable() {
        super.enable()
        isLoaded = true
    }

    override fun createQueue(serverName: String): AbstractQueue {
        return VelocityQueueImpl(serverName, queueScheduler)
    }
}
