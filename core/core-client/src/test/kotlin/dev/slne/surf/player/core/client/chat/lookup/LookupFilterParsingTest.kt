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
    fun `a lookup name resolves to the network uuid before the mojang uuid`() {
        val networkUuid = UUID.randomUUID()
        val mojangUuid = UUID.randomUUID()

        val resolved = runBlocking {
            ChatCommands.resolveLookupPlayer("DevPlayer1", { networkUuid }, { mojangUuid })
        }

        assertEquals(networkUuid, resolved)
    }

    @Test
    fun `a name the network has never seen falls back to mojang`() {
        val mojangUuid = UUID.randomUUID()

        assertEquals(mojangUuid, runBlocking { ChatCommands.resolveLookupPlayer("Notch", { null }, { mojangUuid }) })
        assertNull(runBlocking { ChatCommands.resolveLookupPlayer("Nobody", { null }, { null }) })
    }

    @Test
    fun `sender, receiver and limit end up in the filter`() {
        val filter = (parse(ChatCommands.parseLookupQuery("--sender Keviro --receiver NotAmmo --limit 3"))
                as ParsedLookupFilters.Valid).filter

        assertEquals(keviro, filter.senderUuid)
        assertEquals(ammo, filter.receiverUuid)
        assertEquals(3, filter.limit)
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
