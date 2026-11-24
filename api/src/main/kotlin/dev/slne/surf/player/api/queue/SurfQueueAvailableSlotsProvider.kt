package dev.slne.surf.player.api.queue

import dev.slne.surf.core.api.common.server.SurfServer

/**
 * Provides the number of currently available slots for a given [SurfServer].
 *
 * The provider in use is [SurfQueueApi.availableSlotsProvider].
 */
interface SurfQueueAvailableSlotsProvider {

    /**
     * Returns the number of currently available slots on the given [server].
     *
     * @param server the server whose available slots should be resolved
     * @return the amount of free slots on the server
     */
    suspend fun getAvailableSlots(server: SurfServer): Int
}
