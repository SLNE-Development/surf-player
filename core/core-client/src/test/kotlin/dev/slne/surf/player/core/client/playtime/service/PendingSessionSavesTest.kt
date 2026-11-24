package dev.slne.surf.player.core.client.playtime.service

import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.rabbitmq.api.exception.SurfRabbitConnectionException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class PendingSessionSavesTest {
    private val start = LocalDateTime.of(2026, 11, 24, 12, 0)

    private fun session() = PlaytimeSession(
        playerUuid = UUID.randomUUID(),
        sessionId = UUID.randomUUID(),
        server = "survival01",
        category = "survival",
        startTime = start,
        endTime = start.plusMinutes(30),
    )

    /** A microservice that is down until [up] is set; records every stored session. */
    private class FakeMicroservice {
        var up = false
        val stored = mutableListOf<PlaytimeSession>()

        suspend fun save(session: PlaytimeSession) {
            if (!up) throw SurfRabbitConnectionException("microservice down")
            stored += session
        }
    }

    @Test
    fun `an ended session is saved right away when the microservice is reachable`() = runBlocking {
        val microservice = FakeMicroservice().apply { up = true }
        val saves = PendingSessionSaves(microservice::save)
        val session = session()

        saves.add(session)

        assertTrue(saves.save(session))
        assertEquals(listOf(session), microservice.stored)
        assertEquals(0, saves.size)
    }

    @Test
    fun `a failed save keeps the session and the next retry stores it`() = runBlocking {
        val microservice = FakeMicroservice()
        val saves = PendingSessionSaves(microservice::save)
        val session = session()

        saves.add(session)
        assertFalse(saves.save(session))
        assertTrue(saves.isPending(session.sessionId))

        // Still down: the session survives every retry.
        assertEquals(1, saves.retry())
        assertTrue(microservice.stored.isEmpty())

        microservice.up = true
        assertEquals(0, saves.retry())
        assertEquals(listOf(session), microservice.stored)
        assertEquals(start.plusMinutes(30), microservice.stored.single().endTime)
    }

    @Test
    fun `a stored session is not saved again`() = runBlocking {
        val microservice = FakeMicroservice().apply { up = true }
        val saves = PendingSessionSaves(microservice::save)
        val session = session()

        saves.add(session)
        saves.save(session)
        saves.retry()

        assertTrue(saves.save(session))
        assertEquals(1, microservice.stored.size)
    }

    @Test
    fun `one failing session does not stop the others`() = runBlocking {
        val failing = session()
        val stored = mutableListOf<PlaytimeSession>()
        val saves = PendingSessionSaves {
            if (it == failing) throw IllegalStateException("broken row")
            stored += it
        }
        val others = listOf(session(), session())

        saves.add(failing)
        others.forEach(saves::add)

        assertEquals(1, saves.retry())
        assertEquals(others.toSet(), stored.toSet())
        assertTrue(saves.isPending(failing.sessionId))
    }
}
