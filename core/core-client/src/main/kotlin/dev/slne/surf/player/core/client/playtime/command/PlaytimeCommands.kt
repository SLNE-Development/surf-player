package dev.slne.surf.player.core.client.playtime.command

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.failure
import dev.slne.surf.player.core.client.playtime.config.PlaytimeConfigManager
import dev.slne.surf.player.core.client.playtime.service.PlaytimeService
import dev.slne.surf.player.core.client.playtime.service.PlaytimeStreakService
import net.kyori.adventure.audience.Audience
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * The playtime commands every platform runs; the platforms only parse the arguments.
 */
object PlaytimeCommands {
    private const val UNKNOWN_NAME = "Unbekannt"

    suspend fun showOwn(audience: Audience, player: UUID) {
        audience.sendPlaytimeOverview(
            sessions = PlaytimeService.getAndLoadSessions(player),
            streak = PlaytimeStreakService.loadOrCalculateStreak(player)
        )
    }

    suspend fun showOther(audience: Audience, target: SurfPlayer?): CommandOutcome {
        target ?: return failure(CommandMessages.PLAYER_NOT_FOUND)

        audience.sendPlaytimeOverview(
            sessions = PlaytimeService.getAndLoadSessions(target.uuid),
            streak = PlaytimeStreakService.loadOrCalculateStreak(target.uuid),
            targetName = target.lastKnownName ?: UNKNOWN_NAME
        )
        return CommandOutcome.Silent
    }

    fun reload(): CommandOutcome {
        val millis = measureTimeMillis { PlaytimeConfigManager.reload() }
        return CommandOutcome.Reply(CommandMessages.configReloaded("playtime", millis))
    }

    /** Recalculates the login streak of [target], or of every player when [all] is set. */
    suspend fun recalculate(audience: Audience, target: SurfPlayer?, all: Boolean): CommandOutcome {
        if (all) {
            audience.sendRecalculateAllStarted()
            audience.sendRecalculateAllFinished(PlaytimeStreakService.recalculateAllPlaytimeStreaks())
            return CommandOutcome.Silent
        }

        target ?: return failure(CommandMessages.PLAYER_NOT_FOUND)
        val name = target.lastKnownName ?: UNKNOWN_NAME

        val streak = PlaytimeStreakService.recalculatePlaytimeStreak(target.uuid)
            ?: return failure("Für $name wurden keine Sessions gefunden.")

        PlaytimeStreakService.invalidateCache(target.uuid)
        audience.sendStreakRecalculated(
            playerName = name,
            currentLoginStreak = streak.currentLoginStreak,
            longestLoginStreak = streak.longestLoginStreak
        )
        return CommandOutcome.Silent
    }

    suspend fun createPause(audience: Audience, from: String, to: String): CommandOutcome {
        val startDate = parseDate(from)
        val endDate = parseDate(to)

        if (startDate == null || endDate == null) {
            return failure("Ungültiges Datum. Erlaubte Formate: 31.12.2026 oder 2026-12-31.")
        }
        if (endDate.isBefore(startDate)) return failure("Das Enddatum darf nicht vor dem Startdatum liegen.")

        audience.sendStreakPauseCreated(PlaytimeStreakService.createStreakPause(startDate, endDate))
        return CommandOutcome.Silent
    }

    suspend fun deletePause(audience: Audience, id: Long): CommandOutcome {
        if (!PlaytimeStreakService.deleteStreakPause(id)) {
            return failure("Es wurde keine Streak-Pause mit der ID #$id gefunden.")
        }

        audience.sendStreakPauseDeleted(id)
        return CommandOutcome.Silent
    }

    suspend fun listPauses(audience: Audience): CommandOutcome {
        val pauses = PlaytimeStreakService.loadStreakPauses()
        if (pauses.isEmpty()) return failure("Es sind keine Streak-Pausen eingetragen.")

        audience.sendStreakPauseList(pauses)
        return CommandOutcome.Silent
    }
}
