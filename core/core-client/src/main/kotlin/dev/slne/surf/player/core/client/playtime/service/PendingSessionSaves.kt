package dev.slne.surf.player.core.client.playtime.service

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import kotlinx.coroutines.CancellationException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Ended playtime sessions whose final save has not reached the microservice yet.
 *
 * A session is [add]ed as soon as it ends and only dropped once [save] or [retry] stored it, so a
 * microservice outage while players leave does not lose their playtime: the sessions stay in memory
 * and the next [retry] (every flush) saves them. Saving the same session again is harmless, the
 * microservice replaces an earlier save of it.
 */
class PendingSessionSaves(private val store: suspend (PlaytimeSession) -> Unit) {
    private val pending = ConcurrentHashMap<UUID, PlaytimeSession>()

    /** How many ended sessions still wait for their save. */
    val size: Int get() = pending.size

    /** Whether the session [sessionId] still waits for its save. */
    fun isPending(sessionId: UUID): Boolean = pending.containsKey(sessionId)

    /** Keeps the ended [session] until it was saved. */
    fun add(session: PlaytimeSession) {
        pending[session.sessionId] = session
    }

    /**
     * Saves [session] if it still waits for its save; returns whether it is saved now. A failed save
     * keeps it for the next [retry].
     */
    suspend fun save(session: PlaytimeSession): Boolean {
        if (pending[session.sessionId] !== session) return !isPending(session.sessionId)

        return try {
            store(session)
            pending.remove(session.sessionId, session)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.atWarning()
                .log(
                    "Could not save the ended playtime session %s of %s, retrying with the next flush: %s",
                    session.sessionId, session.playerUuid, e.toString()
                )
            false
        }
    }

    /**
     * Saves every session that still waits for its save; returns how many still wait afterwards.
     */
    suspend fun retry(): Int {
        for (session in pending.values.toList()) {
            save(session)
        }
        return pending.size
    }

    private companion object {
        private val log = logger()
    }
}
