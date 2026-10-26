package dev.slne.surf.minimapcompanion.core.xaero.option

import net.kyori.adventure.key.Key
import kotlin.math.pow

/**
 * The value types Xaero's enforced options have, with the Kotlin types the companion keeps them as.
 */
enum class XaeroOptionType(val description: String) {
    BOOLEAN("boolean"),
    INT("integer"),
    DOUBLE("number"),
    STRING("string"),
    IDENTIFIER_SET("list of keys");

    /**
     * Converts a value read from the config to this type, or returns `null` if it does not fit.
     */
    fun coerce(value: Any): Any? = when (this) {
        BOOLEAN -> value as? Boolean
        INT -> when (value) {
            is Int -> value
            is Long -> value.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
            else -> null
        }

        DOUBLE -> (value as? Number)?.toDouble()
        STRING -> value as? String
        IDENTIFIER_SET -> identifiers(value)
    }

    private fun identifiers(value: Any): Set<String>? {
        val entries = when (value) {
            // Xaero's own config files separate the keys with commas and wrap them in brackets.
            is String -> value.trim().removeSurrounding("[", "]")
                .split(',').map(String::trim).filter(String::isNotEmpty)
            is List<*> -> value.map { it as? String ?: return null }
            else -> return null
        }
        // Xaero reads "overworld" as "minecraft:overworld", so the rules have to as well.
        return entries.takeIf { it.all(Key::parseable) }?.mapTo(LinkedHashSet()) { Key.key(it).asString() }
    }
}

/**
 * One of the options a server can enforce on Xaero's mods, under Xaero's own ID.
 * Values the mod would reject are rejected here, since one rejected value makes the client drop the rest.
 */
class XaeroOption(
    val id: String,
    val type: XaeroOptionType,
    private val validator: (Any) -> Boolean = { true },
) {
    /**
     * Returns whether Xaero accepts [value], which must already have this option's type.
     */
    fun accepts(value: Any) = validator(value)

    companion object {
        fun bool(id: String) = XaeroOption(id, XaeroOptionType.BOOLEAN)

        fun int(id: String, range: IntRange) = XaeroOption(id, XaeroOptionType.INT) { it as Int in range }

        fun int(id: String, min: Int) = XaeroOption(id, XaeroOptionType.INT) { it as Int >= min }

        fun anyInt(id: String) = XaeroOption(id, XaeroOptionType.INT)

        /**
         * An option whose slider positions [indices] stand for the values [value] maps them to.
         */
        fun indexed(id: String, indices: IntRange, value: (Int) -> Int): XaeroOption {
            val values = indices.map(value).toSet()
            return XaeroOption(id, XaeroOptionType.INT) { it in values }
        }

        fun double(id: String) = XaeroOption(id, XaeroOptionType.DOUBLE)

        fun double(id: String, range: ClosedFloatingPointRange<Double>) =
            XaeroOption(id, XaeroOptionType.DOUBLE) { it as Double in range }

        fun positiveDouble(id: String) = XaeroOption(id, XaeroOptionType.DOUBLE) { it as Double > 0.0 }

        fun nonNegativeDouble(id: String) = XaeroOption(id, XaeroOptionType.DOUBLE) { it as Double >= 0.0 }

        fun string(id: String) = XaeroOption(id, XaeroOptionType.STRING)

        /**
         * A set of dimension keys, such as `minecraft:overworld`.
         */
        fun dimensions(id: String) = XaeroOption(id, XaeroOptionType.IDENTIFIER_SET)
    }
}

/**
 * The distance at which Xaero switches to kilometres for slider position [index]: never, always, or a power of ten.
 */
internal fun kilometreThreshold(index: Int) = if (index <= 0) index else 10.0.pow(index - 1).toInt()

/**
 * The options one of Xaero's mods lets a server enforce.
 */
open class XaeroOptionCatalog(vararg options: XaeroOption) {
    val options = options.toList()

    private val byId = this.options.associateBy { it.id }

    operator fun get(id: String) = byId[id]
}
