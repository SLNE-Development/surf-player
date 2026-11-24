package dev.slne.surf.social.core.client.queue.queue

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.social.api.queue.SurfQueueAvailableSlotsProvider
import dev.slne.surf.social.core.client.queue.platform.QueuePlatform

object DefaultSurfQueueAvailableSlotsProvider : SurfQueueAvailableSlotsProvider {
    override suspend fun getAvailableSlots(server: SurfServer): Int {
        return if (SurfServer.current().name == server.name) {
            QueuePlatform.get().maxPlayers() - QueuePlatform.get().onlinePlayerCount()
        } else {
            server.maxPlayers - server.getPlayers().size
        }
    }
}
