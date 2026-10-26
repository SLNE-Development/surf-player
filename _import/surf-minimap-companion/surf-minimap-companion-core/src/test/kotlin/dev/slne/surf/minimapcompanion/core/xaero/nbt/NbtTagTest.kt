package dev.slne.surf.minimapcompanion.core.xaero.nbt

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class NbtTagTest {

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun `compound with a byte is type, name, value and end`() {
        assertContentEquals(
            bytes(10, 1, 0, 1, 'r'.code, 1, 0),
            nbtCompound("r" to NbtByte.of(true)).toNetworkBytes(),
        )
    }

    @Test
    fun `strings use modified utf-8`() {
        assertContentEquals(bytes(8, 0, 2, 0xC0, 0x80), NbtString("\u0000").toNetworkBytes())
    }

    @Test
    fun `empty list has end element type and zero length`() {
        assertContentEquals(bytes(9, 0, 0, 0, 0, 0), NbtList(emptyList()).toNetworkBytes())
    }

    @Test
    fun `list takes the element type of its first element`() {
        assertContentEquals(
            bytes(9, 8, 0, 0, 0, 2, 0, 1, 'a'.code, 0, 1, 'b'.code),
            NbtList(listOf(NbtString("a"), NbtString("b"))).toNetworkBytes(),
        )
    }

    @Test
    fun `int array writes its length and ints`() {
        assertContentEquals(
            bytes(11, 0, 0, 0, 2, 0, 0, 0, 1, -1, -1, -1, -1),
            NbtIntArray(intArrayOf(1, -1)).toNetworkBytes(),
        )
    }

    @Test
    fun `int is four bytes big endian`() {
        assertContentEquals(bytes(3, 1, 2, 3, 4), NbtInt(0x01020304).toNetworkBytes())
    }

    @Test
    fun `double is eight bytes big endian`() {
        assertContentEquals(bytes(6, 0x3F, 0xF0, 0, 0, 0, 0, 0, 0), NbtDouble(1.0).toNetworkBytes())
    }

    @Test
    fun `nested compounds keep their insertion order`() {
        assertContentEquals(
            bytes(10, 10, 0, 1, 'o'.code, 0, 1, 0, 1, 'r'.code, 0, 0),
            nbtCompound("o" to nbtCompound(), "r" to NbtByte.of(false)).toNetworkBytes(),
        )
    }
}
