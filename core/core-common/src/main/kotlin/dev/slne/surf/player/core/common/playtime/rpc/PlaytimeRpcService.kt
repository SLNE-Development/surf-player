package dev.slne.surf.player.core.common.playtime.rpc

import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import dev.slne.surf.player.api.playtime.session.PlaytimeStreakPause
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.time.LocalDate
import java.util.*

/**
 * Playtime sessions, login streaks and streak pauses stored by the microservice.
 *
 * The clients track the running sessions themselves and only persist them through [saveSession].
 */
@RpcService
interface PlaytimeRpcService {

    /**
     * Stores [session], replacing an earlier save of the same session. Sessions shorter than one
     * minute are not stored; returns whether [session] was stored.
     */
    suspend fun saveSession(session: PlaytimeSession): Boolean

    suspend fun loadSessions(playerUuid: UUID): Set<PlaytimeSession>

    suspend fun loadSessionsByServer(playerUuid: UUID, serverName: String): Set<PlaytimeSession>

    suspend fun loadSessionsByCategory(playerUuid: UUID, category: String): Set<PlaytimeSession>

    /** The sum of the stored playtime of [playerUuid] on [serverName] in seconds. */
    suspend fun loadPlaytimeSecondsByServer(playerUuid: UUID, serverName: String): Long

    /** See [dev.slne.surf.player.api.playtime.SurfPlaytimeApi.getPlaytimePerPlayer]. */
    suspend fun loadPlaytimePerPlayer(filter: PlaytimeFilter): Map<UUID, Long>

    suspend fun loadStreak(playerUuid: UUID): PlaytimeStreak?

    /**
     * Stores [streak] as the current login streak of [playerUuid] at [date], raising the longest
     * streak if [streak] exceeds it.
     */
    suspend fun saveStreak(playerUuid: UUID, streak: Int, date: LocalDate)

    /** Calculates the login streak of [playerUuid] up to today from the stored sessions. */
    suspend fun calculateStreak(playerUuid: UUID): Int

    /**
     * Recalculates and stores the login streak of [playerUuid] as of their last login. Returns
     * `null` if [playerUuid] has no sessions.
     */
    suspend fun recalculateStreak(playerUuid: UUID): PlaytimeStreak?

    /** Recalculates and stores the login streaks of all players; returns their number. */
    suspend fun recalculateAllStreaks(): Int

    suspend fun loadStreakPauses(): List<PlaytimeStreakPause>

    suspend fun createStreakPause(startDate: LocalDate, endDate: LocalDate): PlaytimeStreakPause

    /** Deletes the pause with [pauseId]; returns whether it existed. */
    suspend fun deleteStreakPause(pauseId: Long): Boolean
}
