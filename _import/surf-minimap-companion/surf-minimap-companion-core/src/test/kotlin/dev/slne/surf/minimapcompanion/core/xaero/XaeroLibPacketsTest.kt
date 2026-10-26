package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.xaero.nbt.*
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class XaeroLibPacketsTest {

    private fun packet(index: Int, tag: NbtTag) = byteArrayOf(index.toByte()) + tag.toNetworkBytes()

    @Test
    fun `server and dimension handshake are index and the byte one`() {
        assertContentEquals(byteArrayOf(0, 1), XaeroLibPackets.serverHandshake())
        assertContentEquals(byteArrayOf(1, 1), XaeroLibPackets.dimensionHandshake())
    }

    @Test
    fun `config channel names the channel`() {
        assertContentEquals(
            packet(2, nbtCompound("c" to NbtString("xaerominimap:main"))),
            XaeroLibPackets.configChannel("xaerominimap:main"),
        )
    }

    @Test
    fun `enforced config wraps the options and reset flag`() {
        val options = nbtCompound("display_radar" to NbtByte.of(false))
        assertContentEquals(
            packet(3, nbtCompound("o" to options, "r" to NbtByte.of(true))),
            XaeroLibPackets.enforcedConfig(options, reset = true),
        )
    }

    @Test
    fun `profile info reset lists no profiles`() {
        assertContentEquals(
            packet(5, nbtCompound("e" to NbtList(emptyList()), "r" to NbtByte.of(true))),
            XaeroLibPackets.profileInfoReset(),
        )
    }
}
