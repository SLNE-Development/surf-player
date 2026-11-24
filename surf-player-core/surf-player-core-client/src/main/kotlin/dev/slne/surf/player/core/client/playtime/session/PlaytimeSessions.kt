package dev.slne.surf.player.core.client.playtime.session

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.core.client.playtime.afk.AfkService
import dev.slne.surf.player.core.client.playtime.afk.AfkTracker
import dev.slne.surf.player.core.client.playtime.service.PayCheckService
import dev.slne.surf.player.core.client.playtime.service.PlaytimeService
import dev.slne.surf.player.core.client.playtime.service.PlaytimeStreakService
import dev.slne.surf.player.core.client.playtime.streak.LoginStreak
import dev.slne.surf.player.core.client.playtime.streak.isGapBridgedByPauses
import net.kyori.adventure.audience.Audience
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Starts tracking the playtime and afk state of a player that just joined this server.
 */
fun startPlaytimeSession(playerUuid: UUID) {
    PlayerSessionEpochs.begin(playerUuid)
    AfkTracker.track(playerUuid)

    PlaytimeService.cacheSession(newPlaytimeSession(playerUuid, LocalDateTime.now()))
}

/**
 * A new session of [playerUuid] on this server, starting at [now].
 */
fun newPlaytimeSession(playerUuid: UUID, now: LocalDateTime) = PlaytimeSession(
    playerUuid = playerUuid,
    sessionId = UUID.randomUUID(),
    server = SurfCoreApi.getCurrentServerDisplayName(),
    category = SurfCoreApi.getCurrentServerCategory(),
    startTime = now,
    endTime = now,
)

/**
 * Loads the paycheck playtime of a joining player and advances their login streak.
 */
suspend fun handlePlaytimeJoin(playerUuid: UUID, audience: Audience) {
    val epoch = PlayerSessionEpochs.current(playerUuid)

    PayCheckService.cachePlaytime(playerUuid)

    val streak = PlaytimeStreakService.loadPlaytimeStreak(playerUuid)
    val today = LocalDate.now()

    if (streak == null) {
        val calculated = PlaytimeStreakService.calculatePlaytimeStreak(playerUuid) + 1

        cacheStreakIfStillOnServer(playerUuid, epoch, LoginStreak(calculated, calculated))
        PlaytimeStreakService.savePlaytimeStreak(playerUuid, calculated, today)

        audience.sendText {
            appendInfoPrefix()
            info("Deine Login-Streak wurde neu erstellt und auf ")
            variableValue("$calculated Tage")
            info(" berechnet.")
        }

        return
    }

    val lastLogin = streak.lastLoginDate

    val newStreak = when {
        lastLogin == null -> 1
        lastLogin.isEqual(today) -> streak.currentLoginStreak
        lastLogin.plusDays(1).isEqual(today) -> streak.currentLoginStreak + 1
        isGapBridgedByPauses(
            PlaytimeStreakService.loadStreakPauses(),
            lastLogin,
            today
        ) -> streak.currentLoginStreak + 1

        else -> 1
    }

    val newLongest = maxOf(streak.longestLoginStreak, newStreak)

    cacheStreakIfStillOnServer(playerUuid, epoch, LoginStreak(newStreak, newLongest))
    PlaytimeStreakService.savePlaytimeStreak(playerUuid, newStreak, today)
}

/**
 * Drops the per-player caches and the afk tracking of a player that left this server.
 */
fun invalidatePlaytimeCaches(playerUuid: UUID) {
    AfkTracker.forget(playerUuid)

    PlayerSessionEpochs.end(playerUuid) {
        AfkService.changeState(playerUuid, false)
        PayCheckService.invalidateCache(playerUuid)
        PlaytimeStreakService.invalidateCache(playerUuid)
    }
}

/**
 * Closes and persists every active session of a player that left this server.
 */
suspend fun endPlaytimeSessions(playerUuid: UUID) {
    val sessions = PlaytimeService.activeSessionsOf(playerUuid)

    val now = LocalDateTime.now()
    sessions.forEach { session ->
        session.endTime = now
        PlaytimeService.saveSession(session)
        PlaytimeService.removeCachedSession(session.sessionId)
    }
}

private fun cacheStreakIfStillOnServer(playerUuid: UUID, epoch: Long?, streak: LoginStreak) {
    PlayerSessionEpochs.ifCurrent(playerUuid, epoch) {
        PlaytimeStreakService.cacheStreak(playerUuid, streak)
    }
}
