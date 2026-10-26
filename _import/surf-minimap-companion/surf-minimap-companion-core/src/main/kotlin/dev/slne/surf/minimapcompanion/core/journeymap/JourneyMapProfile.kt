package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.minimapcompanion.core.config.JourneyMapConfig

/**
 * What this server enforces on JourneyMap, as the strings JourneyMap's permissions JSON carries:
 * `true`/`false`, enum names and decimal integers. [warnings] lists the configured options that are
 * not sent, or sent unchecked.
 */
data class JourneyMapProfile(
    val values: Map<String, String>,
    val warnings: List<String> = emptyList(),
) {
    companion object {
        private val log = logger()

        private val SERVER_OPTIONS = setOf("ALL", "NONE")
        private val TELEPORT_OPTIONS = setOf("NONE", "ALL", "OPS", "WAYPOINT_ONLY", "GLOBAL_ONLY")

        @Volatile
        private var cached: Pair<JourneyMapConfig, JourneyMapProfile>? = null

        /**
         * Returns the profile [config] describes. Each config instance is parsed once, so its warnings are logged once.
         */
        fun of(config: JourneyMapConfig): JourneyMapProfile {
            cached?.let { (cachedConfig, profile) -> if (cachedConfig === config) return profile }

            val warnings = mutableListOf<String>()
            val profile = parse(config.options.mapValues { it.value.raw() }) { warning ->
                warnings += warning
                log.atWarning().log("%s", warning)
            }.copy(warnings = warnings)
            cached = config to profile
            return profile
        }

        /**
         * Reads the options in [raw] as the config holds them. Known options are checked against what JourneyMap
         * accepts, unknown ones are sent as strings. Each value that cannot be sent is skipped and reported
         * through [warn]; a `null` value enforces nothing.
         */
        fun parse(raw: Map<String, Any?>, warn: (String) -> Unit): JourneyMapProfile {
            val values = LinkedHashMap<String, String>()

            for ((id, value) in raw) {
                if (value == null) continue

                if (id in JourneyMapOptions.COMPANION_OWNED) {
                    warn("Skipping JourneyMap option $id: the companion sets it")
                    continue
                }

                val option = JourneyMapOptions[id]
                if (option == null) {
                    warn("Unknown JourneyMap option $id, sending it as text")
                    values[id] = value.toString()
                    continue
                }

                if (option.type == JourneyMapOptionType.SERVER_OPTION && value.toString().equals("OPS", ignoreCase = true)) {
                    warn("Skipping JourneyMap option $id: OPS needs settings per player, which the companion does not support")
                    continue
                }

                val coerced = coerce(option, value)
                if (coerced == null) {
                    warn("Skipping JourneyMap option $id: $value is not a valid value for it")
                    continue
                }
                values[id] = coerced
            }

            return JourneyMapProfile(values)
        }

        private fun coerce(option: JourneyMapOption, value: Any): String? = when (option.type) {
            JourneyMapOptionType.BOOLEAN -> (value as? Boolean)?.toString()
            JourneyMapOptionType.SERVER_OPTION -> (value as? String)?.uppercase()?.takeIf { it in SERVER_OPTIONS }
            JourneyMapOptionType.TELEPORT_OPTION -> (value as? String)?.uppercase()?.takeIf { it in TELEPORT_OPTIONS }
            JourneyMapOptionType.INT -> when (value) {
                is Int -> value.toLong()
                is Long -> value
                else -> null
            }?.takeIf { option.range == null || it in option.range }?.toString()
        }
    }
}
