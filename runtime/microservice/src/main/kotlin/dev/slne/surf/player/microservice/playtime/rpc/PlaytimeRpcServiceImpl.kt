package dev.slne.surf.player.microservice.playtime.rpc

import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import dev.slne.surf.player.api.playtime.session.PlaytimeStreakPause
import dev.slne.surf.player.core.common.playtime.rpc.LoginStreakUpdate
import dev.slne.surf.player.core.common.playtime.rpc.PlaytimeRpcService
import dev.slne.surf.player.microservice.playtime.db.repository.PlaytimeRepository
import dev.slne.surf.player.microservice.playtime.db.repository.PlaytimeStreakPauseRepository
import dev.slne.surf.player.microservice.playtime.db.repository.PlaytimeStreakRepository
import java.time.LocalDate
import java.util.*

object PlaytimeRpcServiceImpl : PlaytimeRpcService {
    override suspend fun saveSession(session: PlaytimeSession): Boolean =
        PlaytimeRepository.saveSession(session)

    override suspend fun findSessions(playerUuid: UUID): Set<PlaytimeSession> =
        PlaytimeRepository.loadSessions(playerUuid)

    override suspend fun findPlaytimeSeconds(
        playerUuid: UUID,
        server: String?,
        category: String?,
        excludedSessionId: UUID?
    ): Long = PlaytimeRepository.sumPlaytimeSeconds(playerUuid, server, category, excludedSessionId)

    override suspend fun findPlaytimePerPlayer(filter: PlaytimeFilter): Map<UUID, Long> =
        PlaytimeRepository.loadPlaytimePerPlayer(filter)

    override suspend fun findStreak(playerUuid: UUID): PlaytimeStreak? =
        PlaytimeStreakRepository.findStreakInformation(playerUuid)

    override suspend fun calculateStreak(playerUuid: UUID): Int =
        PlaytimeStreakRepository.calculateStreak(playerUuid)

    override suspend fun updateLoginStreak(playerUuid: UUID, today: LocalDate): LoginStreakUpdate =
        PlaytimeStreakRepository.updateLoginStreak(playerUuid, today)

    override suspend fun recalculateStreak(playerUuid: UUID): PlaytimeStreak? =
        PlaytimeStreakRepository.recalculateStreak(playerUuid)

    override suspend fun recalculateAllStreaks(): Int = PlaytimeStreakRepository.recalculateAllStreaks()

    override suspend fun findStreakPauses(): List<PlaytimeStreakPause> =
        PlaytimeStreakPauseRepository.findAllPauses()

    override suspend fun createStreakPause(startDate: LocalDate, endDate: LocalDate): PlaytimeStreakPause =
        PlaytimeStreakPauseRepository.createPause(startDate, endDate)

    override suspend fun deleteStreakPause(pauseId: Long): Boolean =
        PlaytimeStreakPauseRepository.deletePause(pauseId)
}
