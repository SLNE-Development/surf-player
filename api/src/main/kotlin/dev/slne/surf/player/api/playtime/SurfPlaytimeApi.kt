package dev.slne.surf.player.api.playtime

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.playtime.event.AfkStateChangeEvent
import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

private val instance = requiredService<SurfPlaytimeApi>()

/**
 * Playtime of the players, tracked per server session.
 *
 * Changes of the afk state are published as [AfkStateChangeEvent].
 */
interface SurfPlaytimeApi {
    /** The session [playerUuid] currently has on this server, or `null` if they are not online. */
    fun getCurrentPlaytimeSession(playerUuid: UUID): PlaytimeSession?

    /** Whether [playerUuid] is afk on this server. */
    fun isPlayerAfk(playerUuid: UUID): Boolean

    /** The playtime of [playerUuid] on [server] in seconds, including the running session. */
    suspend fun getPlaytimeByServer(playerUuid: UUID, server: String): Long

    /** The playtime of [playerUuid] in [category] in seconds, including the running session. */
    suspend fun getPlaytimeByCategory(playerUuid: UUID, category: String): Long

    /** The playtime of [playerUuid] in seconds, including the running session. */
    suspend fun getTotalPlaytime(playerUuid: UUID): Long

    /** The stored sessions of [playerUuid] together with the running session. */
    suspend fun getAllPlaytimeSessions(playerUuid: UUID): ObjectSet<PlaytimeSession>

    /**
     * Sums the stored playtime in seconds per player of all sessions matching [filter].
     *
     * Only the part of a session inside the window of [filter] is counted. Sessions that are
     * still running are included as far as they have been saved, which happens periodically
     * and when a player leaves a server. Players without matching playtime are not contained.
     */
    suspend fun getPlaytimePerPlayer(filter: PlaytimeFilter): Object2LongMap<UUID>

    /** The stored login streak of [playerUuid], or `null` if none has been stored yet. */
    suspend fun getLoginStreak(playerUuid: UUID): PlaytimeStreak?

    companion object : SurfPlaytimeApi by instance {
        val INSTANCE: SurfPlaytimeApi get() = instance
    }
}
