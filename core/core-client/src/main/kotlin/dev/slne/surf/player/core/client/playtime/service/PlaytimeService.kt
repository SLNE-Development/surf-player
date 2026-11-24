package dev.slne.surf.player.core.client.playtime.service

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.player.core.client.playtime.afk.AfkService
import dev.slne.surf.player.core.client.playtime.rpc.playtimeRpcService
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongMaps
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val MAX_CONCURRENT_FLUSHES = 32

private val log = logger()

/**
 * The playtime sessions running on this server and access to the stored ones.
 */
object PlaytimeService {
    private val sessionsById = ConcurrentHashMap<UUID, PlaytimeSession>()
    private val sessionsByPlayer = ConcurrentHashMap<UUID, List<PlaytimeSession>>()

    /**
     * Live view of every session currently tracked on this server.
     *
     * Iterating this does not copy, but it is only weakly consistent with concurrent changes.
     * Use [activePlaytimeSessions] when a stable snapshot is required.
     */
    val activeSessionsView: Collection<PlaytimeSession> =
        Collections.unmodifiableCollection(sessionsById.values)

    /**
     * Snapshot of every session currently tracked on this server.
     */
    val activePlaytimeSessions: Set<PlaytimeSession> get() = sessionsById.values.toSet()

    /**
     * The sessions [playerUuid] currently has on this server.
     */
    fun activeSessionsOf(playerUuid: UUID): List<PlaytimeSession> =
        sessionsByPlayer[playerUuid] ?: emptyList()

    /**
     * The session [playerUuid] currently has on this server, or `null`.
     */
    fun currentSession(playerUuid: UUID): PlaytimeSession? =
        activeSessionsOf(playerUuid).firstOrNull()

    fun cacheSession(session: PlaytimeSession) {
        sessionsById[session.sessionId] = session

        sessionsByPlayer.compute(session.playerUuid) { _, sessions ->
            when {
                sessions == null -> listOf(session)
                sessions.any { it.sessionId == session.sessionId } -> sessions
                else -> sessions + session
            }
        }
    }

    fun removeCachedSession(sessionId: UUID) {
        val removed = sessionsById.remove(sessionId) ?: return

        sessionsByPlayer.computeIfPresent(removed.playerUuid) { _, sessions ->
            val remaining = sessions.filter { it.sessionId != sessionId }

            remaining.ifEmpty { null }
        }
    }

    suspend fun saveSession(session: PlaytimeSession) {
        playtimeRpcService.saveSession(session)
    }

    private val pendingSaves = PendingSessionSaves { saveSession(it) }

    /**
     * Ends the running [session] at [endTime]: it is no longer tracked as running and is kept in
     * memory until [saveEndedSession] or the next [flushAll] stored it, so a failed save (e.g. while
     * the microservice is down) does not lose it.
     */
    fun endSession(session: PlaytimeSession, endTime: LocalDateTime) {
        session.endTime = endTime
        pendingSaves.add(session)
        removeCachedSession(session.sessionId)
    }

    /**
     * Saves the [session] ended with [endSession]; returns whether it is stored now. If not, the
     * next [flushAll] tries again.
     */
    suspend fun saveEndedSession(session: PlaytimeSession): Boolean = pendingSaves.save(session)

    /** How many ended sessions still wait for their save. */
    val pendingSaveCount: Int get() = pendingSaves.size

    suspend fun loadSessions(playerUuid: UUID): ObjectSet<PlaytimeSession> =
        playtimeRpcService.findSessions(playerUuid).toObjectSet()

    /**
     * The stored playtime of [playerUuid] in seconds, restricted to [server] and [category] where
     * given, without the stored save of the session [excludedSessionId].
     */
    suspend fun loadPlaytimeSeconds(
        playerUuid: UUID,
        server: String? = null,
        category: String? = null,
        excludedSessionId: UUID? = null
    ): Long = playtimeRpcService.findPlaytimeSeconds(playerUuid, server, category, excludedSessionId)

    suspend fun loadPlaytimePerPlayer(filter: PlaytimeFilter): Object2LongMap<UUID> =
        Object2LongMaps.unmodifiable(
            Object2LongOpenHashMap(playtimeRpcService.findPlaytimePerPlayer(filter))
        )

    /**
     * The stored sessions of [playerUuid] together with their running session on this server.
     */
    suspend fun getAndLoadSessions(playerUuid: UUID): ObjectSet<PlaytimeSession> {
        val activeSession = currentSession(playerUuid)
        val loadedSessions = loadSessions(playerUuid)

        val result = mutableObjectSetOf<PlaytimeSession>(loadedSessions.size + 1)

        if (activeSession != null) {
            result.add(activeSession)

            for (session in loadedSessions) {
                if (session.sessionId != activeSession.sessionId) {
                    result.add(session)
                }
            }
        } else {
            result.addAll(loadedSessions)
        }

        return result
    }

    /**
     * Saves every running session up to now, and every ended session whose save failed before. A
     * failed save of one session neither stops the others nor loses it: a running session is saved
     * again with the next flush anyway, an ended one stays pending.
     */
    suspend fun flushAll(): Unit = coroutineScope {
        val sessions = activePlaytimeSessions

        if (sessions.isNotEmpty()) {
            val semaphore = Semaphore(MAX_CONCURRENT_FLUSHES)

            for (session in sessions) {
                launch {
                    semaphore.withPermit {
                        // A session that ended meanwhile keeps its end time.
                        if (sessionsById.containsKey(session.sessionId)) {
                            session.endTime = LocalDateTime.now()
                        }
                        try {
                            saveSession(session)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            log.atWarning().log(
                                "Could not save the playtime session %s of %s, retrying with the next flush: %s",
                                session.sessionId, session.playerUuid, e.toString()
                            )
                        }
                    }
                }
            }
        }

        pendingSaves.retry()
    }

    suspend fun updateAllActiveSessions() {
        val now = LocalDateTime.now()

        for (session in activeSessionsView) {
            session.endTime = now

            val playerUuid = session.playerUuid
            if (!AfkService.isAfk(playerUuid)) {
                PayCheckService.increasePlaytime(playerUuid, 1)
            }
        }
    }
}
