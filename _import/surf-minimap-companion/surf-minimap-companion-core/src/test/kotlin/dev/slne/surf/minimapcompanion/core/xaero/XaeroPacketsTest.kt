package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.xaero.nbt.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XaeroPacketsTest {
    private val uuid = UUID(0x0000000100000002, 0x0000000300000004)
    private val uuidTag = NbtIntArray(intArrayOf(1, 2, 3, 4))

    private fun packet(index: Int, tag: NbtTag) = byteArrayOf(index.toByte()) + tag.toNetworkBytes()

    @Test
    fun `level id payload is packet index zero followed by the id big endian`() {
        assertContentEquals(byteArrayOf(0, 1, 2, 3, 4), XaeroPackets.levelId(0x01020304))
    }

    @Test
    fun `negative level ids keep all four bytes`() {
        assertContentEquals(byteArrayOf(0, -1, -1, -1, -1), XaeroPackets.levelId(-1))
    }

    @Test
    fun `handshake announces network version three`() {
        assertContentEquals(byteArrayOf(1, 0, 0, 0, 3), XaeroPackets.handshake())
    }

    @Test
    fun `minimap rules carry cave, nether cave and radar flags`() {
        val expected = nbtCompound(
            "cm" to NbtByte.of(true),
            "ncm" to NbtByte.of(false),
            "r" to NbtByte.of(true),
        )
        assertContentEquals(packet(4, expected), XaeroPackets.minimapRules(caveMode = true, netherCaveMode = false, radar = true))
    }

    @Test
    fun `world map rules have no radar flag`() {
        val expected = nbtCompound("cm" to NbtByte.of(false), "ncm" to NbtByte.of(true))
        assertContentEquals(packet(4, expected), XaeroPackets.worldMapRules(caveMode = false, netherCaveMode = true))
    }

    @Test
    fun `tracked player carries position and dimension`() {
        val expected = nbtCompound(
            "r" to NbtByte.of(false),
            "i" to uuidTag,
            "x" to NbtDouble(1.5),
            "y" to NbtDouble(64.0),
            "z" to NbtDouble(-3.25),
            "d" to NbtString("minecraft:the_nether"),
        )
        assertContentEquals(
            packet(2, expected),
            XaeroPackets.trackedPlayer(uuid, 1.5, 64.0, -3.25, "minecraft:the_nether"),
        )
    }

    @Test
    fun `removed tracked player carries only the flag and uuid`() {
        val expected = nbtCompound("r" to NbtByte.of(true), "i" to uuidTag)
        assertContentEquals(packet(2, expected), XaeroPackets.trackedPlayerRemoved(uuid))
    }

    @Test
    fun `tracker reset is the bare index`() {
        assertContentEquals(byteArrayOf(3), XaeroPackets.trackerReset())
    }

    @Test
    fun `xaero channels are recognised`() {
        assertTrue(XaeroChannels.isXaero("xaeroworldmap:main"))
        assertTrue(XaeroChannels.isXaero("xaerominimap:main"))
        assertTrue(XaeroChannels.isXaero("xaerolib:main"))
        assertFalse(XaeroChannels.isXaero("minecraft:brand"))
    }

    @Test
    fun `only the minimap and world map channels are map channels`() {
        assertTrue(XaeroChannels.isMap("xaeroworldmap:main"))
        assertTrue(XaeroChannels.isMap("xaerominimap:main"))
        assertFalse(XaeroChannels.isMap("xaerolib:main"))
    }
}
