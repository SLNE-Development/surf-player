package dev.slne.surf.player.core.client.chat.lookup

import dev.slne.surf.player.core.client.chat.command.ChatCommands
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.failure
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.UUID

class LookupFilterParsingTest {
    private val keviro = UUID.randomUUID()
    private val ammo = UUID.randomUUID()
    private val known = mapOf("keviro" to keviro, "notammo" to ammo)

    private val resolve: suspend (String) -> UUID? = { known[it.lowercase()] }

    private fun parse(query: Map<String, String>) = runBlocking { query.parseFilters(resolve) }

    @Test
    fun `known players are resolved into the filter`() {
        val parsed = parse(mapOf("--sender" to "Keviro", "--receiver" to "NotAmmo", "--deletedBy" to "keviro"))

        val filter = (parsed as ParsedLookupFilters.Valid).filter
        assertEquals(keviro, filter.senderUuid)
        assertEquals(ammo, filter.receiverUuid)
        assertEquals(keviro, filter.deletedBy)
    }

    @Test
    fun `a query without player filters has none`() {
        val filter = (parse(mapOf("--server" to "lobby")) as ParsedLookupFilters.Valid).filter

        assertNull(filter.senderUuid)
        assertNull(filter.receiverUuid)
        assertNull(filter.deletedBy)
    }

    @ParameterizedTest
    @ValueSource(strings = ["--sender", "--receiver", "--deletedBy"])
    fun `an unknown player in any player filter is reported`(key: String) {
        assertEquals(
            ParsedLookupFilters.UnknownPlayer(key, "NichtExistent123"),
            parse(mapOf(key to "NichtExistent123"))
        )
    }

    @Test
    fun `an empty player filter is an unknown player`() {
        assertTrue(parse(mapOf("--sender" to "")) is ParsedLookupFilters.UnknownPlayer)
    }

    @Test
    fun `the lookup fails with the shared message and runs no query for an unknown player`() {
        var queried = false

        val outcome = runBlocking {
            ChatCommands.lookup(mapOf("--sender" to "Keviro", "--receiver" to "NichtExistent123"), resolve) {
                queried = true
                emptyList()
            }
        }

        assertEquals(failure(CommandMessages.PLAYER_NOT_FOUND), outcome)
        assertFalse(queried)
    }

    @Test
    fun `the lookup queries with the resolved filter`() {
        var queriedSender: UUID? = null

        runBlocking {
            ChatCommands.lookup(mapOf("--sender" to "Keviro"), resolve) {
                queriedSender = it.senderUuid
                emptyList()
            }
        }

        assertEquals(keviro, queriedSender)
    }
}
