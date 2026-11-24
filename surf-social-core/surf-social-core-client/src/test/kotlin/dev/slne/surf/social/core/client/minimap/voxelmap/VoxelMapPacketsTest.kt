package dev.slne.surf.minimapcompanion.core.voxelmap

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class VoxelMapPacketsTest {
    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun `settings frames the json after a zero byte`() {
        assertContentEquals(bytes(0, 2, '{'.code, '}'.code), VoxelMapPackets.settings("{}"))
    }

    @Test
    fun `long json gets a two byte length`() {
        val payload = VoxelMapPackets.settings("a".repeat(200))
        assertContentEquals(bytes(0xC8, 0x01), payload.copyOfRange(1, 3))
        assertEquals(1 + 2 + 200, payload.size)
    }
}
