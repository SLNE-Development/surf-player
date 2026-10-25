package dev.slne.surf.minimapcompanion.minestom.platform

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegisteredChannelsTest {
    private val uuid = UUID.fromString("00000000-0000-0000-0000-000000000001")

    @AfterTest
    fun cleanUp() = RegisteredChannels.forget(uuid)

    @Test
    fun `register payload is split on null bytes`() {
        val payload = "xaeroworldmap:main\u0000xaerominimap:main".toByteArray(Charsets.UTF_8)
        assertEquals(listOf("xaeroworldmap:main", "xaerominimap:main"), RegisteredChannels.parseRegisterPayload(payload))
    }

    @Test
    fun `empty entries in the register payload are dropped`() {
        val payload = "\u0000xaerominimap:main\u0000".toByteArray(Charsets.UTF_8)
        assertEquals(listOf("xaerominimap:main"), RegisteredChannels.parseRegisterPayload(payload))
    }

    @Test
    fun `registered channels are remembered per player`() {
        RegisteredChannels.register(uuid, listOf("xaerominimap:main"))
        assertTrue(RegisteredChannels.isRegistered(uuid, "xaerominimap:main"))
    }

    @Test
    fun `isRegistered is false for a channel that was never registered`() {
        RegisteredChannels.register(uuid, listOf("xaerominimap:main"))
        assertFalse(RegisteredChannels.isRegistered(uuid, "xaeroworldmap:main"))
    }

    @Test
    fun `forget drops all channels of the player`() {
        RegisteredChannels.register(uuid, listOf("xaerominimap:main"))
        RegisteredChannels.forget(uuid)
        assertFalse(RegisteredChannels.isRegistered(uuid, "xaerominimap:main"))
    }
}
