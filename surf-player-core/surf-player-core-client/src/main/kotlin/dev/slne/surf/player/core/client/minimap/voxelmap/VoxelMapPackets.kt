package dev.slne.surf.player.core.client.minimap.voxelmap

import java.io.ByteArrayOutputStream

/**
 * Builds the payloads VoxelMap's client reads. VoxelMap disconnects on bytes it does not read,
 * so nothing follows the last field.
 */
object VoxelMapPackets {

    /**
     * The settings [json], after VoxelMap's packet ID 0, with its length as a VarInt.
     */
    fun settings(json: String): ByteArray {
        val utf8 = json.toByteArray(Charsets.UTF_8)
        val bytes = ByteArrayOutputStream(utf8.size + 6)
        bytes.write(0)
        bytes.writeVarInt(utf8.size)
        bytes.write(utf8)
        return bytes.toByteArray()
    }

    private fun ByteArrayOutputStream.writeVarInt(value: Int) {
        var remaining = value
        while (remaining and 0x7F.inv() != 0) {
            write(remaining and 0x7F or 0x80)
            remaining = remaining ushr 7
        }
        write(remaining)
    }
}
