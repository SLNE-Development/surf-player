package dev.slne.surf.minimapcompanion.core.xaero

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XaeroPacketsTest {

    @Test
    fun `level id payload is packet index zero followed by the id big endian`() {
        assertContentEquals(byteArrayOf(0, 1, 2, 3, 4), XaeroPackets.levelId(0x01020304))
    }

    @Test
    fun `negative level ids keep all four bytes`() {
        assertContentEquals(byteArrayOf(0, -1, -1, -1, -1), XaeroPackets.levelId(-1))
    }

    @Test
    fun `only the two xaero channels are recognised`() {
        assertTrue(XaeroChannels.isXaero("xaeroworldmap:main"))
        assertTrue(XaeroChannels.isXaero("xaerominimap:main"))
        assertFalse(XaeroChannels.isXaero("minecraft:brand"))
    }
}
