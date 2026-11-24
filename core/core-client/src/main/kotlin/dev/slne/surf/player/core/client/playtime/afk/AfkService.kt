package dev.slne.surf.player.core.client.playtime.afk

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * The published afk state of the players on this server, see [AfkTracker.applyState].
 */
object AfkService {
    private val afkPlayers = ConcurrentHashMap.newKeySet<UUID>()

    fun changeState(uuid: UUID, afk: Boolean): Boolean {
        return if (afk) {
            afkPlayers.add(uuid)
        } else {
            afkPlayers.remove(uuid)
        }
    }

    fun isAfk(uuid: UUID) = afkPlayers.contains(uuid)
}
