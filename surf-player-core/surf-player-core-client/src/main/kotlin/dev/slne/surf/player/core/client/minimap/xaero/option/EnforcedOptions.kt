package dev.slne.surf.social.core.client.minimap.xaero.option

import net.kyori.adventure.nbt.*

/**
 * The option values a server enforces on one of Xaero's mods, keyed by Xaero's option IDs.
 * Values are [Boolean], [Int], [Double], [String] or a [Set] of key strings.
 */
class EnforcedOptions(val values: Map<String, Any>) {

    fun boolean(id: String) = values[id] as? Boolean

    @Suppress("UNCHECKED_CAST")
    fun identifiers(id: String) = values[id] as? Set<String>

    /**
     * Encodes the values the way Xaero's Lib syncs them.
     */
    fun toNbt(): CompoundBinaryTag = CompoundBinaryTag.from(values.mapValues { (_, value) -> tagOf(value) })

    private fun tagOf(value: Any): BinaryTag = when (value) {
        is Boolean -> ByteBinaryTag.byteBinaryTag(if (value) 1 else 0)
        is Int -> IntBinaryTag.intBinaryTag(value)
        is Double -> DoubleBinaryTag.doubleBinaryTag(value)
        is String -> StringBinaryTag.stringBinaryTag(value)
        is Set<*> -> ListBinaryTag.listBinaryTag(BinaryTagTypes.STRING, value.map { StringBinaryTag.stringBinaryTag(it as String) })
        else -> error("Unsupported option value $value")
    }

    companion object {
        val EMPTY = EnforcedOptions(emptyMap())

        /**
         * Reads the options in [raw] as the config holds them. Values [catalog] knows are checked against it,
         * unknown options are sent with the type their value has. Each value that cannot be sent is
         * skipped and reported through [warn]; a `null` value enforces nothing.
         */
        fun parse(catalog: XaeroOptionCatalog, raw: Map<String, Any?>, warn: (String) -> Unit): EnforcedOptions {
            val values = LinkedHashMap<String, Any>()

            for ((id, value) in raw) {
                if (value == null) continue

                val option = catalog[id]
                if (option == null) {
                    val type = infer(value)
                    if (type == null) {
                        warn("Skipping unknown Xaero option $id: cannot send $value")
                        continue
                    }

                    warn("Unknown Xaero option $id, sending it as ${type.description}")
                    values[id] = type.coerce(value)!!
                    continue
                }

                val coerced = option.type.coerce(value)?.takeIf(option::accepts)
                if (coerced == null) {
                    warn("Skipping Xaero option $id: $value is not a valid ${option.type.description} for it")
                    continue
                }

                values[id] = coerced
            }

            return EnforcedOptions(values)
        }

        private fun infer(value: Any) = when (value) {
            is Boolean -> XaeroOptionType.BOOLEAN
            is Int, is Long -> XaeroOptionType.INT
            is Float, is Double -> XaeroOptionType.DOUBLE
            is String -> XaeroOptionType.STRING
            is List<*> -> XaeroOptionType.IDENTIFIER_SET
            else -> null
        }?.takeIf { it.coerce(value) != null }
    }
}
