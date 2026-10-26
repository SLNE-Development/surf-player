package dev.slne.surf.minimapcompanion.core.worldinfo

import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class WorldInfoTest {
    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun `name joins the world key's value and the unsigned level id`() {
        assertEquals("overworld-2320127456", WorldInfo.name(Key.key("minecraft:overworld"), -1974839840))
        assertEquals("space_lobby-829346366", WorldInfo.name(Key.key("surf:space_lobby"), 829346366))
    }

    @Test
    fun `characters outside a to z, digits and underscore become underscores`() {
        assertEquals("events_maze_v2-1", WorldInfo.name(Key.key("surf:events/maze.v2"), 1))
    }

    @Test
    fun `long world names are cut to 48 characters`() {
        assertEquals("a".repeat(48) + "-7", WorldInfo.name(Key.key("surf", "a".repeat(60)), 7))
    }

    @Test
    fun `payload has a one byte length`() {
        assertContentEquals(bytes(0, 42, 5) + "lobby".toByteArray(), WorldInfo.payload("lobby"))
    }
}
