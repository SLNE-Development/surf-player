package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.xaero.nbt.*
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
        RULES, nbtCompound(
            "cm" to NbtByte.of(caveMode),
            "ncm" to NbtByte.of(netherCaveMode),
            "r" to NbtByte.of(radar),
        )
    )

    fun worldMapRules(caveMode: Boolean, netherCaveMode: Boolean) = packet(
        RULES, nbtCompound(
            "cm" to NbtByte.of(caveMode),
            "ncm" to NbtByte.of(netherCaveMode),
        )
    )

    /**
     * Shows the player with [uuid] at the given position in [dimension], a world key such as `minecraft:overworld`.
     */
    fun trackedPlayer(uuid: UUID, x: Double, y: Double, z: Double, dimension: String) = packet(
        TRACKED_PLAYER, nbtCompound(
            "r" to NbtByte.of(false),
            "i" to uuidTag(uuid),
            "x" to NbtDouble(x),
            "y" to NbtDouble(y),
            "z" to NbtDouble(z),
            "d" to NbtString(dimension),
        )
    )

    fun trackedPlayerRemoved(uuid: UUID) = packet(
        TRACKED_PLAYER, nbtCompound(
            "r" to NbtByte.of(true),
            "i" to uuidTag(uuid),
        )
    )

    /**
     * Removes every tracked player the client knows.
     */
    fun trackerReset() = byteArrayOf(TRACKER_RESET)

    // The layout of Minecraft's UUIDUtil.uuidToIntArray.
    private fun uuidTag(uuid: UUID) = NbtIntArray(
        intArrayOf(
            (uuid.mostSignificantBits ushr 32).toInt(),
            uuid.mostSignificantBits.toInt(),
            (uuid.leastSignificantBits ushr 32).toInt(),
            uuid.leastSignificantBits.toInt(),
        )
    )

    private fun packet(index: Int, tag: NbtTag) = byteArrayOf(index.toByte()) + tag.toNetworkBytes()
}
