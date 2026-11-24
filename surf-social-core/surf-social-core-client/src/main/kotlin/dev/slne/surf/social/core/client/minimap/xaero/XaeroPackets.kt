package dev.slne.surf.social.core.client.minimap.xaero

import dev.slne.surf.social.core.client.minimap.xaero.nbt.toNetworkBytes
import dev.slne.surf.social.core.client.minimap.xaero.nbt.uuidTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.nio.ByteBuffer
import java.util.*

/**
 * Builds the payloads Xaero's World Map and Minimap read: a packet index byte followed by the packet data.
 * Both mods use the same indices.
 */
object XaeroPackets {
    private const val LEVEL_ID: Byte = 0
    private const val HANDSHAKE: Byte = 1
    private const val TRACKED_PLAYER = 2
    private const val TRACKER_RESET: Byte = 3
    private const val RULES = 4

    /**
     * The network version both mods announce in their handshake.
     */
    const val NETWORK_VERSION = 3

    /**
     * The packet that tells the client which level ID the current world has.
     */
    fun levelId(id: Int): ByteArray = ByteBuffer.allocate(5)
        .put(LEVEL_ID)
        .putInt(id)
        .array()

    /**
     * The packet that tells the client the server has the mod.
     */
    fun handshake(): ByteArray = ByteBuffer.allocate(5)
        .put(HANDSHAKE)
        .putInt(NETWORK_VERSION)
        .array()

    fun minimapRules(caveMode: Boolean, netherCaveMode: Boolean, radar: Boolean) = packet(
        RULES, CompoundBinaryTag.builder()
            .putBoolean("cm", caveMode)
            .putBoolean("ncm", netherCaveMode)
            .putBoolean("r", radar)
            .build()
    )

    fun worldMapRules(caveMode: Boolean, netherCaveMode: Boolean) = packet(
        RULES, CompoundBinaryTag.builder()
            .putBoolean("cm", caveMode)
            .putBoolean("ncm", netherCaveMode)
            .build()
    )

    /**
     * Shows the player with [uuid] at the given position in [dimension], a world key such as `minecraft:overworld`.
     */
    fun trackedPlayer(uuid: UUID, x: Double, y: Double, z: Double, dimension: String) = packet(
        TRACKED_PLAYER, CompoundBinaryTag.builder()
            .putBoolean("r", false)
            .put("i", uuidTag(uuid))
            .putDouble("x", x)
            .putDouble("y", y)
            .putDouble("z", z)
            .putString("d", dimension)
            .build()
    )

    fun trackedPlayerRemoved(uuid: UUID) = packet(
        TRACKED_PLAYER, CompoundBinaryTag.builder()
            .putBoolean("r", true)
            .put("i", uuidTag(uuid))
            .build()
    )

    /**
     * Removes every tracked player the client knows.
     */
    fun trackerReset() = byteArrayOf(TRACKER_RESET)

    private fun packet(index: Int, tag: CompoundBinaryTag) = byteArrayOf(index.toByte()) + tag.toNetworkBytes()
}
