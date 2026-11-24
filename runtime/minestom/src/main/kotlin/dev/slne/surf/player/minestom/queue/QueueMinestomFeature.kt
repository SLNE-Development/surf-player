package dev.slne.surf.player.minestom.queue

import dev.slne.surf.player.core.client.queue.ClientQueueSystem
import dev.slne.surf.player.core.common.queue.QueueSystem
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.queue.command.queueAdminActions

/**
 * The server queue (formerly the `surf-queue-minestom` plugin).
 */
object QueueMinestomFeature : MinestomPlayerFeature {
    override val id = "queue"

    override suspend fun onLoad() {
        ClientQueueSystem.load(dataDirectory)
    }

    override suspend fun onEnable() {
        ClientQueueSystem.enable()
        QueueSystem.isLoaded = true
    }

    override suspend fun onDisable() {
        ClientQueueSystem.disable()
    }

    override fun adminActions() = queueAdminActions()
}
