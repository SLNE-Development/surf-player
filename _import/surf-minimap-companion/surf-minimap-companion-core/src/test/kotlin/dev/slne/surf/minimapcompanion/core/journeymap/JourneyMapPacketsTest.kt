package dev.slne.surf.minimapcompanion.core.journeymap

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class JourneyMapPacketsTest {
    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun `permissions frames the json with flags that claim nothing`() {
        assertContentEquals(bytes(42, 0, 2, '{'.code, '}'.code, 0, 0), JourneyMapPackets.permissions("{}"))
    }

    @Test
    fun `long json gets a two byte length`() {
        val payload = JourneyMapPackets.permissions("a".repeat(200))
        assertContentEquals(bytes(0xC8, 0x01), payload.copyOfRange(2, 4))
        assertEquals(2 + 2 + 200 + 2, payload.size)
    }

    @Test
    fun `remove player writes the uuid`() {
        assertContentEquals(
            bytes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
            JourneyMapPackets.removePlayer(UUID(0x0102030405060708, 0x090A0B0C0D0E0F10)),
        )
    }

    @Test
    fun `player locations list visible players with position and hidden ones without`() {
        val a = UUID(1, 2)
        val b = UUID(3, 4)
        val payload = JourneyMapPackets.playerLocations(
            linkedMapOf(
                a to JourneyMapPlayer(300, 1.5, 64.0, -2.25, 90f, -45f, "minecraft:overworld"),
                b to null,
            )
        )

        assertContentEquals(
            bytes(2) +
                bytes(1) + bytes(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2) +
                bytes(0xAC, 0x02) +
                bytes(0x3F, 0xF8, 0, 0, 0, 0, 0, 0) +
                bytes(0x40, 0x50, 0, 0, 0, 0, 0, 0) +
                bytes(0xC0, 0x02, 0, 0, 0, 0, 0, 0) +
                bytes(64, 0xE0) +
                bytes(0) + bytes(0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 4),
            payload,
        )
    }

    @Test
    fun `entries encoded on their own join to the same list`() {
        val a = UUID(1, 2)
        val b = UUID(3, 4)
        val posA = JourneyMapPlayer(300, 1.5, 64.0, -2.25, 90f, -45f, "minecraft:overworld")
        val posB = JourneyMapPlayer(7, -8.0, 70.0, 3.0, 0f, 10f, "minecraft:overworld")

        assertContentEquals(
            JourneyMapPackets.playerLocations(linkedMapOf(a to posA, b to posB)),
            JourneyMapPackets.playerLocations(listOf(JourneyMapPackets.playerEntry(a, posA), JourneyMapPackets.playerEntry(b, posB))),
        )
    }
}
