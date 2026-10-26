package dev.slne.surf.minimapcompanion.core.xaero.nbt

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/**
 * The NBT tags Xaero's packets use, written the way Minecraft sends NBT over the network.
 */
sealed interface NbtTag {
    val id: Int

    fun writePayload(out: DataOutputStream)
}

data class NbtByte(val value: Byte) : NbtTag {
    override val id get() = 1

    override fun writePayload(out: DataOutputStream) = out.writeByte(value.toInt())

    companion object {
        fun of(value: Boolean) = NbtByte(if (value) 1 else 0)
    }
}

data class NbtInt(val value: Int) : NbtTag {
    override val id get() = 3

    override fun writePayload(out: DataOutputStream) = out.writeInt(value)
}

data class NbtDouble(val value: Double) : NbtTag {
    override val id get() = 6

    override fun writePayload(out: DataOutputStream) = out.writeDouble(value)
}

data class NbtString(val value: String) : NbtTag {
    override val id get() = 8

    // writeUTF writes the modified UTF-8 NBT expects.
    override fun writePayload(out: DataOutputStream) = out.writeUTF(value)
}

/**
 * A list of tags, which NBT requires to share one type.
 */
data class NbtList(val elements: List<NbtTag>) : NbtTag {
    override val id get() = 9

    override fun writePayload(out: DataOutputStream) {
        out.writeByte(elements.firstOrNull()?.id ?: 0)
        out.writeInt(elements.size)
        for (element in elements) {
            element.writePayload(out)
        }
    }
}

data class NbtCompound(val entries: Map<String, NbtTag>) : NbtTag {
    override val id get() = 10

    override fun writePayload(out: DataOutputStream) {
        for ((name, tag) in entries) {
            out.writeByte(tag.id)
            out.writeUTF(name)
            tag.writePayload(out)
        }
        out.writeByte(0)
    }
}

class NbtIntArray(val values: IntArray) : NbtTag {
    override val id get() = 11

    override fun writePayload(out: DataOutputStream) {
        out.writeInt(values.size)
        for (value in values) {
            out.writeInt(value)
        }
    }
}

fun nbtCompound(vararg entries: Pair<String, NbtTag>) = NbtCompound(linkedMapOf(*entries))

/**
 * Writes [tag] as network NBT: its type, then its payload, without a root name.
 */
fun DataOutputStream.writeNbt(tag: NbtTag) {
    writeByte(tag.id)
    tag.writePayload(this)
}

fun NbtTag.toNetworkBytes(): ByteArray {
    val bytes = ByteArrayOutputStream()
    DataOutputStream(bytes).use { it.writeNbt(this) }
    return bytes.toByteArray()
}
