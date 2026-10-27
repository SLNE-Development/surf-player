package dev.slne.surf.minimapcompanion.minestom.listener

import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapChannels
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.minestom.platform.RegisteredChannels
import net.minestom.server.coordinate.Pos
import net.minestom.server.network.packet.server.common.PluginMessagePacket
import net.minestom.server.network.packet.server.play.JoinGamePacket
import net.minestom.server.network.packet.server.play.UpdateViewPositionPacket
import net.minestom.testing.Env
import net.minestom.testing.EnvTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@EnvTest
class ChannelAnnounceListenerTest {

    // VoxelMap asks for the world ID only on its first tick in a new world, and only if the channel was announced
    // by then. Minestom spawns a player whose chunks still load only on a later tick, starting with the view position,
    // so the announcement has to come with the join game packet instead.
    @Test
    fun `channels are announced with the join, before the spawn`(env: Env) {
        ChannelAnnounceListener.register(env.process().eventHandler())
        val connection = env.createConnection()
        val packets = connection.trackIncoming()

        connection.connect(env.createFlatInstance(), Pos(0.0, 41.0, 0.0))

        val sent = packets.collect()
        val register = sent.filterIsInstance<PluginMessagePacket>().filter { it.channel() == "minecraft:register" }
        assertEquals(1, register.size)
        assertEquals(
            XaeroChannels.ALL + JourneyMapChannels.ANNOUNCED + VoxelMapChannels.SETTINGS,
            RegisteredChannels.parseRegisterPayload(register.single().data()),
        )

        val announced = sent.indexOf(register.single())
        assertTrue(sent.indexOfFirst { it is JoinGamePacket } < announced, "announced before the join game packet")
        assertTrue(announced < sent.indexOfFirst { it is UpdateViewPositionPacket }, "announced only once spawned")
    }
}
