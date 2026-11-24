package dev.slne.surf.player.microservice.connection.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class MinecraftAccountIdsTest {
    private val uuid = UUID.fromString("7cd111b0-cd94-3eae-97a4-c5bd35d362f7")

    @Test
    fun `matches the uuid with or without dashes in either case`() {
        assertEquals(
            listOf(
                "7cd111b0-cd94-3eae-97a4-c5bd35d362f7",
                "7cd111b0cd943eae97a4c5bd35d362f7",
                "7CD111B0-CD94-3EAE-97A4-C5BD35D362F7",
                "7CD111B0CD943EAE97A4C5BD35D362F7",
            ),
            minecraftAccountIds(uuid)
        )
    }
}
