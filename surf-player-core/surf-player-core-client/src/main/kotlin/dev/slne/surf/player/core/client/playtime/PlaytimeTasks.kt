package dev.slne.surf.player.core.client.playtime

import dev.slne.surf.api.core.util.runAtFixedRate
import dev.slne.surf.player.core.client.playtime.afk.AfkTracker
import dev.slne.surf.player.core.client.playtime.service.PlaytimeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * The repeating playtime tasks of a server: counting the running sessions, saving them
 * periodically and detecting afk players.
 */
object PlaytimeTasks {
    private val jobs = mutableListOf<Job>()

    @Synchronized
    fun start(scope: CoroutineScope) {
        jobs += scope.runAtFixedRate(1.seconds, taskName = "playtime-update") {
            PlaytimeService.updateAllActiveSessions()
        }
        jobs += scope.runAtFixedRate(5.minutes, taskName = "playtime-flush") {
            PlaytimeService.flushAll()
        }
        jobs += scope.runAtFixedRate(1.seconds, taskName = "afk-check") {
            AfkTracker.checkIdlePlayers()
        }
    }

    /**
     * Stops the tasks and saves every running session one last time.
     */
    suspend fun stop() {
        synchronized(this) {
            jobs.forEach { it.cancel() }
            jobs.clear()
        }

        PlaytimeService.flushAll()
    }
}
