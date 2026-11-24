package dev.slne.surf.player.core.client.playtime.afk

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.emptyObjectSet
import dev.slne.surf.player.api.playtime.event.AfkStateChangeEvent
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.core.client.playtime.platform.PlaytimePlatform
import dev.slne.surf.player.core.client.playtime.service.PlaytimeService
import dev.slne.surf.player.core.client.playtime.session.newPlaytimeSession
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes

/**
 * The idle state of one tracked player.
 */
private class AfkState(@Volatile var lastActivityNanos: Long) {
    val afk = AtomicBoolean(false)
}

/**
 * Tracks how long each player has been idle and turns that into afk state changes.
 */
object AfkTracker {
    private val afkTimeNanos = 3.minutes.inWholeNanoseconds

    private val states = ConcurrentHashMap<UUID, AfkState>()

    /**
     * Starts tracking [playerUuid] as active.
     */
    fun track(playerUuid: UUID) {
        states[playerUuid] = AfkState(System.nanoTime())

        AfkService.changeState(playerUuid, false)
    }

    /**
     * Stops tracking [playerUuid].
     */
    fun forget(playerUuid: UUID) {
        states.remove(playerUuid)
    }

    /**
     * Records activity of [playerUuid], bringing them back out of the afk state if needed.
     * Called by the platforms whenever a player looks around.
     */
    fun recordActivity(playerUuid: UUID) {
        if (markActive(playerUuid)) {
            applyState(playerUuid, false)
        }
    }

    /**
     * Moves every player that has been idle for long enough into the afk state.
     */
    fun checkIdlePlayers() {
        val now = System.nanoTime()

        for (uuid in idlePlayers(now)) {
            if (!markAfkIfStillIdle(uuid, now)) continue

            if (PlaytimePlatform.onlinePlayer(uuid) == null) {
                forget(uuid)
                continue
            }

            applyState(uuid, true)
        }
    }

    /**
     * Records activity of [playerUuid] and returns whether they were afk until now.
     */
    fun markActive(playerUuid: UUID): Boolean {
        val state = states[playerUuid] ?: return false

        state.lastActivityNanos = System.nanoTime()

        return state.afk.get() && state.afk.compareAndSet(true, false)
    }

    /**
     * Returns the players that have been idle long enough to be considered afk at [now].
     */
    @Suppress("JavaMapForEach")
    fun idlePlayers(now: Long): Set<UUID> {
        var idle: ObjectOpenHashSet<UUID>? = null

        states.forEach { uuid, state ->
            if (!state.afk.get() && now - state.lastActivityNanos >= afkTimeNanos) {
                (idle ?: ObjectOpenHashSet<UUID>().also { idle = it }).add(uuid)
            }
        }

        return idle ?: emptyObjectSet()
    }

    /**
     * Marks [playerUuid] as afk unless they became active again since [now] was taken, and
     * returns whether the state actually changed.
     */
    fun markAfkIfStillIdle(playerUuid: UUID, now: Long): Boolean {
        val state = states[playerUuid] ?: return false

        // Recheck — player may have moved by now
        if (now - state.lastActivityNanos < afkTimeNanos) return false
        if (!state.afk.compareAndSet(false, true)) return false

        // Activity may have been recorded between the recheck and the flag flip. Undo the
        // transition instead of sending a player that just moved into afk.
        if (now - state.lastActivityNanos < afkTimeNanos) {
            state.afk.compareAndSet(true, false)
            return false
        }

        return true
    }

    /**
     * Publishes the tracked afk state of [playerUuid]: updates their playtime session, fires an
     * [AfkStateChangeEvent] and tells them about the change.
     *
     * Returns `false` when the tracked state no longer matches [isAfk], which happens when the
     * state changed again since the change was scheduled.
     */
    fun applyState(playerUuid: UUID, isAfk: Boolean): Boolean {
        // Check that the state is still the same, as it may have changed since the task was scheduled
        val state = states[playerUuid] ?: return false
        if (state.afk.get() != isAfk) return false

        AfkService.changeState(playerUuid, isAfk)
        updatePlaytimeSession(playerUuid, isAfk)

        AfkStateChangeEvent(playerUuid, isAfk).call()

        PlaytimePlatform.onlinePlayer(playerUuid)?.sendText {
            appendInfoPrefix()
            info("Du bist nun ")

            if (isAfk) {
                info("AFK.")
            } else {
                info("nicht mehr AFK.")
            }
        }

        return true
    }

    private fun updatePlaytimeSession(playerUuid: UUID, isAfk: Boolean) {
        val now = LocalDateTime.now()
        val activeSessions = PlaytimeService.activeSessionsOf(playerUuid)

        if (isAfk) {
            activeSessions.forEach { endSession(it, now) }
            return
        }

        if (activeSessions.isEmpty()) {
            PlaytimeService.cacheSession(newPlaytimeSession(playerUuid, now))
            return
        }

        // Clean up unexpected duplicate sessions, keep only the most recent one
        activeSessions
            .sortedByDescending { it.startTime }
            .drop(1)
            .forEach { endSession(it, now) }
    }

    private fun endSession(session: PlaytimeSession, now: LocalDateTime) {
        session.endTime = now
        PlaytimeService.removeCachedSession(session.sessionId)

        PlaytimePlatform.launchAsync {
            PlaytimeService.saveSession(session)
        }
    }
}
