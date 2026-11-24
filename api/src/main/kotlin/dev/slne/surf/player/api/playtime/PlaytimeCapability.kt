package dev.slne.surf.player.api.playtime

import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

/** Playtime, sessions, streaks and AFK state of a single player. See [SurfPlaytimeApi]. */
interface PlaytimeCapability {
    val uuid: UUID

    /** Whether this player is afk on this server. */
    val isAfk: Boolean get() = SurfPlaytimeApi.isPlayerAfk(uuid)

    /** The session this player currently has on this server, or `null` if they are not online. */
    fun playtimeSession(): PlaytimeSession? = SurfPlaytimeApi.getCurrentPlaytimeSession(uuid)

    /** The playtime of this player in seconds, including the running session. */
    suspend fun totalPlaytime(): Long = SurfPlaytimeApi.getTotalPlaytime(uuid)

    /** The playtime of this player on [server] in seconds, including the running session. */
    suspend fun playtimeOnServer(server: String): Long =
        SurfPlaytimeApi.getPlaytimeByServer(uuid, server)

    /** The playtime of this player in [category] in seconds, including the running session. */
    suspend fun playtimeInCategory(category: String): Long =
        SurfPlaytimeApi.getPlaytimeByCategory(uuid, category)

    /** The stored sessions of this player together with the running session. */
    suspend fun playtimeSessions(): ObjectSet<PlaytimeSession> =
        SurfPlaytimeApi.getAllPlaytimeSessions(uuid)

    /** The stored login streak of this player, or `null` if none has been stored yet. */
    suspend fun loginStreak(): PlaytimeStreak? = SurfPlaytimeApi.getLoginStreak(uuid)
}
