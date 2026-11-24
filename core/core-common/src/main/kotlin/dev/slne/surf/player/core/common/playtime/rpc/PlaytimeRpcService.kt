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

    /** Every stored session of [playerUuid]. */
    suspend fun findSessions(playerUuid: UUID): Set<PlaytimeSession>

    /**
     * The summed duration in seconds of the stored sessions of [playerUuid], only counting sessions
     * on [server] and in [category] where given, and leaving out the session [excludedSessionId]
     * (the running session the caller adds itself).
     */
    suspend fun findPlaytimeSeconds(
        playerUuid: UUID,
        server: String?,
        category: String?,
        excludedSessionId: UUID?
    ): Long

    /** See [dev.slne.surf.player.api.playtime.SurfPlaytimeApi.getPlaytimePerPlayer]. */
    suspend fun findPlaytimePerPlayer(filter: PlaytimeFilter): Map<UUID, Long>

    suspend fun findStreak(playerUuid: UUID): PlaytimeStreak?

    /** Calculates the login streak of [playerUuid] up to today from the stored sessions, without storing it. */
    suspend fun calculateStreak(playerUuid: UUID): Int

    /**
     * Advances the stored login streak of [playerUuid] for a login on [today] (the date of the
     * client, which may live in another time zone than the microservice) and stores it.
     */
    suspend fun updateLoginStreak(playerUuid: UUID, today: LocalDate): LoginStreakUpdate

    /**
     * Recalculates and stores the login streak of [playerUuid] as of their last login. Returns
     * `null` if [playerUuid] has no sessions.
     */
    suspend fun recalculateStreak(playerUuid: UUID): PlaytimeStreak?

    /** Recalculates and stores the login streaks of all players; returns their number. */
    suspend fun recalculateAllStreaks(): Int

    suspend fun findStreakPauses(): List<PlaytimeStreakPause>

    suspend fun createStreakPause(startDate: LocalDate, endDate: LocalDate): PlaytimeStreakPause

    /** Deletes the pause with [pauseId]; returns whether it existed. */
    suspend fun deleteStreakPause(pauseId: Long): Boolean
}
