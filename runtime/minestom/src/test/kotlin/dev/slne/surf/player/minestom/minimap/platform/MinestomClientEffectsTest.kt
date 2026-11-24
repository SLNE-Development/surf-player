package dev.slne.surf.player.minestom.minimap.platform

import net.minestom.server.network.ConnectionState
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.packet.PacketWriting
import net.minestom.server.network.packet.server.BufferedPacket
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.EntityEffectPacket
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertContentEquals

/**
 * The client effect packets must match Minestom's own effect packets byte for byte, or a Minestom update that
 * changes them would kick players instead of failing here.
 */
class MinestomClientEffectsTest {
    private val entityId = 7
    private val effect = PotionEffect.SPEED

    private fun BufferedPacket.bytes() = ByteArray(length().toInt()).also { buffer().copyTo(index(), it, 0, it.size) }

    private fun framed(packet: ServerPacket, compressionThreshold: Int): ByteArray {
        val buffer = NetworkBuffer.resizableBuffer()
        PacketWriting.writeFramedPacket(buffer, ConnectionState.PLAY, packet, compressionThreshold)
        return ByteArray(buffer.writeIndex().toInt()).also { buffer.copyTo(0, it, 0, it.size) }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 256])
    fun `add packet matches minestom's effect packet`(compressionThreshold: Int) {
        val minestom = EntityEffectPacket(entityId, Potion(effect, 0, Potion.INFINITE_DURATION, Potion.AMBIENT_FLAG))
        assertContentEquals(
            framed(minestom, compressionThreshold),
            MinestomClientEffects.addPacket(entityId, effect.id(), compressionThreshold).bytes(),
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 256])
    fun `remove packet matches minestom's remove effect packet`(compressionThreshold: Int) {
        val minestom = RemoveEntityEffectPacket(entityId, effect)
        assertContentEquals(
            framed(minestom, compressionThreshold),
            MinestomClientEffects.removePacket(entityId, effect.id(), compressionThreshold).bytes(),
        )
    }
}
