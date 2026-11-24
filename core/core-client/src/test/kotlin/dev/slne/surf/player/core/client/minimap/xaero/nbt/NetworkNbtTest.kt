package dev.slne.surf.player.core.client.minimap.xaero.nbt

import net.kyori.adventure.nbt.CompoundBinaryTag
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContentEquals

class NetworkNbtTest {

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun `compound is written as type and payload, without a root name`() {
        assertContentEquals(
            bytes(10, 1, 0, 1, 'r'.code, 1, 0),
            CompoundBinaryTag.builder().putBoolean("r", true).build().toNetworkBytes(),
        )
    }

    @Test
    fun `strings use modified utf-8`() {
        assertContentEquals(
            bytes(10, 8, 0, 1, 's'.code, 0, 2, 0xC0, 0x80, 0),
            CompoundBinaryTag.builder().putString("s", "\u0000").build().toNetworkBytes(),
        )
    }

    @Test
    fun `uuid tag uses minecraft's int array layout`() {
        assertContentEquals(intArrayOf(1, 2, 3, 4), uuidTag(UUID(0x0000000100000002, 0x0000000300000004)).value())
    }
}
