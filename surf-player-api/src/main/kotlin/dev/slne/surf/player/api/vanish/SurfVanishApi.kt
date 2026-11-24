package dev.slne.surf.player.api.vanish

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

private val instance = requiredService<SurfVanishApi>()

interface SurfVanishApi {

    /**
     * Whether the player identified by [uuid] is currently vanished anywhere in the network.
     */
    fun isVanished(uuid: UUID): Boolean

    /**
     * The UUIDs of all vanished players in the network, including players on other servers.
     */
    fun vanishedPlayers(): ObjectSet<UUID>

    /**
     * The UUIDs of all vanished players that are online on this server.
     */
    fun onlineVanishedPlayers(): ObjectSet<UUID>

    companion object : SurfVanishApi by instance {
        val INSTANCE get() = instance
    }
}
