package dev.slne.surf.player.minestom.queue

import dev.slne.surf.player.core.client.queue.ClientQueueInstance
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.queue.command.queueCommand

/**
 * The server queue (formerly the `surf-queue-minestom` plugin).
 */
object QueueMinestomFeature : MinestomPlayerFeature {
    override val id = "queue"

    override suspend fun onLoad() {
        ClientQueueInstance.get().load()
    }

    override suspend fun onEnable() {
        ClientQueueInstance.get().enable()
        queueCommand()
    }

    override suspend fun onDisable() {
        ClientQueueInstance.get().disable()
    }
}
