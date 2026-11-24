package dev.slne.surf.player.core.client.playtime.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.playtime.SurfPlaytimeApi
import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import dev.slne.surf.player.core.client.playtime.afk.AfkService
import dev.slne.surf.player.core.client.playtime.service.PlaytimeService
import dev.slne.surf.player.core.client.playtime.service.PlaytimeStreakService
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

@AutoService(SurfPlaytimeApi::class)
class SurfPlaytimeApiImpl : SurfPlaytimeApi {
    override fun getCurrentPlaytimeSession(playerUuid: UUID): PlaytimeSession? =
        PlaytimeService.currentSession(playerUuid)

    override fun isPlayerAfk(playerUuid: UUID): Boolean = AfkService.isAfk(playerUuid)

    override suspend fun getPlaytimeByServer(playerUuid: UUID, server: String): Long {
        val currentSession = getCurrentPlaytimeSession(playerUuid)?.takeIf { it.server == server }
        val stored = PlaytimeService.loadPlaytimeSeconds(
            playerUuid,
            server = server,
            excludedSessionId = currentSession?.sessionId
        )

        return stored + (currentSession?.durationSeconds ?: 0)
    }

    override suspend fun getPlaytimeByCategory(playerUuid: UUID, category: String): Long {
        val currentSession = getCurrentPlaytimeSession(playerUuid)?.takeIf { it.category == category }
        val stored = PlaytimeService.loadPlaytimeSeconds(
            playerUuid,
            category = category,
            excludedSessionId = currentSession?.sessionId
        )

        return stored + (currentSession?.durationSeconds ?: 0)
    }

    override suspend fun getTotalPlaytime(playerUuid: UUID): Long =
        PlaytimeService.getAndLoadSessions(playerUuid).sumOf { it.durationSeconds }

    override suspend fun getAllPlaytimeSessions(playerUuid: UUID): ObjectSet<PlaytimeSession> =
        PlaytimeService.getAndLoadSessions(playerUuid)

    override suspend fun getPlaytimePerPlayer(filter: PlaytimeFilter): Object2LongMap<UUID> =
        PlaytimeService.loadPlaytimePerPlayer(filter)

    override suspend fun getLoginStreak(playerUuid: UUID): PlaytimeStreak? =
        PlaytimeStreakService.loadPlaytimeStreak(playerUuid)
}
