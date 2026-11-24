package dev.slne.surf.player.core.client.queue.queue

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.api.queue.SurfQueueAvailableSlotsProvider
import dev.slne.surf.player.core.client.queue.platform.QueuePlatform

object DefaultSurfQueueAvailableSlotsProvider : SurfQueueAvailableSlotsProvider {
    override suspend fun getAvailableSlots(server: SurfServer): Int {
        return if (SurfServer.current().name == server.name) {
            QueuePlatform.maxPlayers() - QueuePlatform.onlinePlayerCount()
        } else {
            server.maxPlayers - server.getPlayers().size
        }
    }
}
