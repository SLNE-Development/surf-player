package dev.slne.surf.social.core.client.minimap.xaero.nbt

import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.IntArrayBinaryTag
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Writes this compound the way Minecraft sends NBT over the network: its type, then its payload, without a root name.
 */
fun CompoundBinaryTag.toNetworkBytes(): ByteArray {
    val bytes = ByteArrayOutputStream()
    BinaryTagIO.writer().writeNameless(this, bytes)
    return bytes.toByteArray()
}

/**
 * [uuid] in the layout of Minecraft's UUIDUtil.uuidToIntArray.
 */
fun uuidTag(uuid: UUID): IntArrayBinaryTag = IntArrayBinaryTag.intArrayBinaryTag(
    (uuid.mostSignificantBits ushr 32).toInt(),
    uuid.mostSignificantBits.toInt(),
    (uuid.leastSignificantBits ushr 32).toInt(),
    uuid.leastSignificantBits.toInt(),
)
