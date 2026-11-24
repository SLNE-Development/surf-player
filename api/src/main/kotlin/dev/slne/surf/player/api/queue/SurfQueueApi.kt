package dev.slne.surf.player.api.queue

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.server.SurfServer

private val instance = requiredService<SurfQueueApi>()

/**
 * The queues of players waiting to join a server of the network.
 *
 * Every server has exactly one [SurfQueue]; it is created on first access.
 */
interface SurfQueueApi {

    /** The queue of players waiting to join the server named [serverName]. */
    fun queue(serverName: String): SurfQueue

    /** The queue of players waiting to join [server]. */
    fun queue(server: SurfServer): SurfQueue = queue(server.name)

    /**
     * Resolves how many players may currently be sent to a server. Game servers use it to decide
     * how many queued players to transfer; replace it to customise that number.
     */
    var availableSlotsProvider: SurfQueueAvailableSlotsProvider

    companion object : SurfQueueApi by instance {
        val INSTANCE: SurfQueueApi get() = instance
    }
}
