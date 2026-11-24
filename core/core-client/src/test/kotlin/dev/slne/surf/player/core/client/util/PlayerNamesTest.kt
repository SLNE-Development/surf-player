package dev.slne.surf.player.core.client.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.*

class PlayerNamesTest {
    private val offlineUuid = UUID.nameUUIDFromBytes("OfflinePlayer:DevPlayer1".toByteArray())

    @Test
    fun `the first source that knows the player wins`() = runBlocking {
        val asked = mutableListOf<String>()
        val sources = listOf<suspend (UUID) -> String?>(
            { asked += "surf-core"; "DevPlayer1" },
            { asked += "mojang"; "Somebody" },
        )

        assertEquals("DevPlayer1", firstResolved(offlineUuid, sources))
        assertEquals(listOf("surf-core"), asked)
    }

    @Test
    fun `an offline uuid unknown to Mojang is still named by surf-core`() = runBlocking {
        val sources = listOf<suspend (UUID) -> String?>(
            { uuid -> if (uuid == offlineUuid) "DevPlayer1" else null },
            { null },
        )

        assertEquals("DevPlayer1", firstResolved(offlineUuid, sources))
    }

    @Test
    fun `a failing source is skipped`() = runBlocking {
        val sources = listOf<suspend (UUID) -> String?>(
            { error("surf-core unreachable") },
            { "DevPlayer1" },
        )

        assertEquals("DevPlayer1", firstResolved(offlineUuid, sources))
    }

    @Test
    fun `nothing is resolved when no source knows the player`() = runBlocking {
        val sources = listOf<suspend (UUID) -> String?>({ null }, { null })

        assertNull(firstResolved(offlineUuid, sources))
    }
}
