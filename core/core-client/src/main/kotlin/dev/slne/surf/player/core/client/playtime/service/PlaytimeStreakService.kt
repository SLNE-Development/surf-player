package dev.slne.surf.player.core.client.playtime.service

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import dev.slne.surf.player.api.playtime.session.PlaytimeStreakPause
import dev.slne.surf.player.core.client.playtime.rpc.playtimeRpcService
import dev.slne.surf.player.core.client.playtime.streak.LoginStreak
import dev.slne.surf.player.core.common.playtime.rpc.LoginStreakUpdate
import java.time.LocalDate
import java.util.*

private const val MAX_CACHED_STREAKS = 16_384L

/**
 * Login streaks and streak pauses, with a cache of the streaks of the players on this server.
 */
object PlaytimeStreakService {
    private val streakCache = Caffeine.newBuilder()
        .maximumSize(MAX_CACHED_STREAKS)
        .build<UUID, LoginStreak>()

    suspend fun loadPlaytimeStreak(playerUuid: UUID): PlaytimeStreak? =
        playtimeRpcService.findStreak(playerUuid)

    suspend fun calculatePlaytimeStreak(playerUuid: UUID): Int =
        playtimeRpcService.calculateStreak(playerUuid)

    /** Advances and stores the streak of [playerUuid] for a login on [today]. */
    suspend fun updateLoginStreak(playerUuid: UUID, today: LocalDate): LoginStreakUpdate =
        playtimeRpcService.updateLoginStreak(playerUuid, today)

    suspend fun recalculatePlaytimeStreak(playerUuid: UUID): PlaytimeStreak? =
        playtimeRpcService.recalculateStreak(playerUuid)

    suspend fun recalculateAllPlaytimeStreaks(): Int = playtimeRpcService.recalculateAllStreaks()

    suspend fun loadStreakPauses(): List<PlaytimeStreakPause> = playtimeRpcService.findStreakPauses()

    suspend fun createStreakPause(startDate: LocalDate, endDate: LocalDate): PlaytimeStreakPause =
        playtimeRpcService.createStreakPause(startDate, endDate)

    suspend fun deleteStreakPause(pauseId: Long): Boolean =
        playtimeRpcService.deleteStreakPause(pauseId)

    /**
     * The cached streak of [playerUuid], otherwise the stored one, otherwise a freshly calculated
     * one; the result is cached.
     */
    suspend fun loadOrCalculateStreak(playerUuid: UUID): LoginStreak {
        getStreak(playerUuid)?.let { return it }

        val loaded = loadPlaytimeStreak(playerUuid)
        val streak = if (loaded != null) {
            LoginStreak(loaded.currentLoginStreak, loaded.longestLoginStreak)
        } else {
            val calculated = calculatePlaytimeStreak(playerUuid)
            LoginStreak(calculated, calculated)
        }

        cacheStreak(playerUuid, streak)
        return streak
    }

    fun cacheStreak(playerUuid: UUID, streak: LoginStreak) {
        streakCache.put(playerUuid, streak)
    }

    fun getStreak(playerUuid: UUID): LoginStreak? = streakCache.getIfPresent(playerUuid)

    fun invalidateCache(playerUuid: UUID) {
        streakCache.invalidate(playerUuid)
    }
}
