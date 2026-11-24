package dev.slne.surf.minimapcompanion.core.xaero.nbt

import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag

/**
 * Reads a Xaero packet back: its index byte and the network NBT after it. Compounds keep no key order in their bytes,
 * so tests compare the decoded tag.
 */
fun decodedPacket(payload: ByteArray): Pair<Byte, CompoundBinaryTag> =
    payload[0] to BinaryTagIO.reader().readNameless(payload.inputStream().apply { skip(1) })
