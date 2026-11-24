package dev.slne.surf.social.core.client.minimap.xaero

import dev.slne.surf.social.core.client.minimap.xaero.nbt.decodedPacket
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class XaeroLibPacketsTest {

    private fun packet(index: Int, tag: CompoundBinaryTag) = index.toByte() to tag

    @Test
    fun `server and dimension handshake are index and the byte one`() {
        assertContentEquals(byteArrayOf(0, 1), XaeroLibPackets.serverHandshake())
        assertContentEquals(byteArrayOf(1, 1), XaeroLibPackets.dimensionHandshake())
    }

    @Test
    fun `config channel names the channel`() {
        assertEquals(
            packet(2, CompoundBinaryTag.builder().putString("c", "xaerominimap:main").build()),
            decodedPacket(XaeroLibPackets.configChannel("xaerominimap:main")),
        )
    }

    @Test
    fun `enforced config wraps the options and reset flag`() {
        val options = CompoundBinaryTag.builder().putBoolean("display_radar", false).build()
        assertEquals(
            packet(3, CompoundBinaryTag.builder().put("o", options).putBoolean("r", true).build()),
            decodedPacket(XaeroLibPackets.enforcedConfig(options, reset = true)),
        )
    }

    @Test
    fun `profile info reset lists no profiles`() {
        assertEquals(
            packet(5, CompoundBinaryTag.builder().put("e", ListBinaryTag.empty()).putBoolean("r", true).build()),
            decodedPacket(XaeroLibPackets.profileInfoReset()),
        )
    }
}
