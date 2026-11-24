package dev.slne.surf.social.minestom.minimap.platform

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.packet.PacketVanilla
import net.minestom.server.network.packet.PacketWriting
import net.minestom.server.network.packet.server.BufferedPacket
import net.minestom.server.network.packet.server.play.EntityEffectPacket
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect

/**
 * Sends status effects only the client knows, such as the ones mods add. Minestom's effect packets only take
 * effects Minestom knows, so these write the same layout with a raw effect ID and frame it as Minestom does.
 */
object MinestomClientEffects {
    // Minecraft's own effects take the first IDs, so the effects mods add start after them.
    val vanillaEffectCount = PotionEffect.values().size

    private val ADD_ID = PacketVanilla.SERVER_PACKET_PARSER.play().packetInfo(EntityEffectPacket::class.java).id()
    private val REMOVE_ID = PacketVanilla.SERVER_PACKET_PARSER.play().packetInfo(RemoveEntityEffectPacket::class.java).id()

    private data class ClientEffect(val entityId: Int, val networkId: Int)

    // Like EntityEffectPacket for an endless effect hidden from the HUD: entity, effect, amplifier, duration, flags.
    private val ADD = object : NetworkBuffer.Type<ClientEffect> {
        override fun write(buffer: NetworkBuffer, value: ClientEffect) {
            buffer.write(NetworkBuffer.VAR_INT, value.entityId)
            buffer.write(NetworkBuffer.VAR_INT, value.networkId)
            buffer.write(NetworkBuffer.VAR_INT, 0)
            buffer.write(NetworkBuffer.VAR_INT, Potion.INFINITE_DURATION)
            buffer.write(NetworkBuffer.BYTE, Potion.AMBIENT_FLAG)
        }

        override fun read(buffer: NetworkBuffer) = throw UnsupportedOperationException("Client effects are only sent")
    }

    // Like RemoveEntityEffectPacket: entity, effect.
    private val REMOVE = object : NetworkBuffer.Type<ClientEffect> {
        override fun write(buffer: NetworkBuffer, value: ClientEffect) {
            buffer.write(NetworkBuffer.VAR_INT, value.entityId)
            buffer.write(NetworkBuffer.VAR_INT, value.networkId)
        }

        override fun read(buffer: NetworkBuffer) = throw UnsupportedOperationException("Client effects are only sent")
    }

    fun add(player: Player, networkId: Int) {
        player.sendPacket(addPacket(player.entityId, networkId))
    }

    fun remove(player: Player, networkId: Int) {
        player.sendPacket(removePacket(player.entityId, networkId))
    }

    // Minestom compresses every player connection once the threshold is above zero.
    internal fun addPacket(entityId: Int, networkId: Int, compressionThreshold: Int = MinecraftServer.getCompressionThreshold()) =
        framed(ADD, ADD_ID, ClientEffect(entityId, networkId), compressionThreshold)

    internal fun removePacket(entityId: Int, networkId: Int, compressionThreshold: Int = MinecraftServer.getCompressionThreshold()) =
        framed(REMOVE, REMOVE_ID, ClientEffect(entityId, networkId), compressionThreshold)

    private fun framed(type: NetworkBuffer.Type<ClientEffect>, packetId: Int, effect: ClientEffect, compressionThreshold: Int): BufferedPacket {
        val buffer = NetworkBuffer.resizableBuffer()
        PacketWriting.writeFramedPacket(buffer, type, packetId, effect, compressionThreshold)
        return BufferedPacket(buffer, 0, buffer.writeIndex())
    }
}
