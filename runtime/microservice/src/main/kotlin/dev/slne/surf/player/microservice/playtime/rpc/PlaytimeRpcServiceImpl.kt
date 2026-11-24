package dev.slne.surf.player.microservice.playtime.rpc

import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import dev.slne.surf.player.api.playtime.session.PlaytimeStreakPause
import dev.slne.surf.player.core.common.playtime.rpc.PlaytimeRpcService
import dev.slne.surf.player.microservice.playtime.db.repository.PlaytimeRepository
import dev.slne.surf.player.microservice.playtime.db.repository.PlaytimeStreakPauseRepository
import dev.slne.surf.player.microservice.playtime.db.repository.PlaytimeStreakRepository
import java.time.LocalDate
import java.util.*

object PlaytimeRpcServiceImpl : PlaytimeRpcService {
    override suspend fun saveSession(session: PlaytimeSession): Boolean =
        PlaytimeRepository.saveSession(session)

    override suspend fun loadSessions(playerUuid: UUID): Set<PlaytimeSession> =
        PlaytimeRepository.loadSessions(playerUuid)

    override suspend fun loadSessionsByServer(playerUuid: UUID, serverName: String): Set<PlaytimeSession> =
        PlaytimeRepository.loadSessionsByServer(playerUuid, serverName)

    override suspend fun loadSessionsByCategory(playerUuid: UUID, category: String): Set<PlaytimeSession> =
        PlaytimeRepository.loadSessionsByCategory(playerUuid, category)

    override suspend fun loadPlaytimeSecondsByServer(playerUuid: UUID, serverName: String): Long =
        PlaytimeRepository.loadPlaytimeSecondsByServer(playerUuid, serverName)

    override suspend fun loadPlaytimePerPlayer(filter: PlaytimeFilter): Map<UUID, Long> =
        PlaytimeRepository.loadPlaytimePerPlayer(filter)

    override suspend fun loadStreak(playerUuid: UUID): PlaytimeStreak? =
        PlaytimeStreakRepository.findStreakInformation(playerUuid)

    override suspend fun saveStreak(playerUuid: UUID, streak: Int, date: LocalDate) {
        PlaytimeStreakRepository.saveStreak(playerUuid, streak, date)
    }

    override suspend fun calculateStreak(playerUuid: UUID): Int =
        PlaytimeStreakRepository.calculateStreak(playerUuid)

    override suspend fun recalculateStreak(playerUuid: UUID): PlaytimeStreak? =
        PlaytimeStreakRepository.recalculateStreak(playerUuid)

    override suspend fun recalculateAllStreaks(): Int = PlaytimeStreakRepository.recalculateAllStreaks()

    override suspend fun loadStreakPauses(): List<PlaytimeStreakPause> =
        PlaytimeStreakPauseRepository.findAllPauses()

    override suspend fun createStreakPause(startDate: LocalDate, endDate: LocalDate): PlaytimeStreakPause =
        PlaytimeStreakPauseRepository.createPause(startDate, endDate)

    override suspend fun deleteStreakPause(pauseId: Long): Boolean =
        PlaytimeStreakPauseRepository.deletePause(pauseId)
}
