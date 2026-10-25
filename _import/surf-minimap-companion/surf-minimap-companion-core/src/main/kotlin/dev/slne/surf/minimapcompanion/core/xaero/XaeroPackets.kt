package dev.slne.surf.minimapcompanion.core.xaero

import java.nio.ByteBuffer

/**
 * Builds the payloads Xaero's mods read: a packet index byte followed by the packet data.
 */
object XaeroPackets {
    private const val LEVEL_ID: Byte = 0

    /**
     * The packet that tells the client which level ID the current world has.
     */
    fun levelId(id: Int): ByteArray = ByteBuffer.allocate(5)
        .put(LEVEL_ID)
        .putInt(id)
        .array()
}
