package dev.slne.surf.social.core.client.minimap.journeymap

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.social.core.client.minimap.config.JourneyMapConfig
import dev.slne.surf.social.core.client.minimap.global.GlobalSettings

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
        private var cached: Triple<JourneyMapConfig, GlobalSettings, JourneyMapProfile>? = null

        /**
         * Returns the profile [config] describes, with the entries [global] sets applied. Each pair of instances is
         * parsed once, so its warnings and the options the global settings override are logged once.
         */
        fun of(config: JourneyMapConfig, global: GlobalSettings = GlobalSettings.NONE): JourneyMapProfile {
            cached?.let { (cachedConfig, cachedGlobal, profile) ->
                if (cachedConfig === config && cachedGlobal === global) return profile
            }

            val options = JourneyMapGlobalSettings.apply(config.options.mapValues { it.value.raw() }, global) { hint ->
                log.atInfo().log("%s", hint)
            }

            val warnings = mutableListOf<String>()
            val profile = parse(options) { warning ->
                warnings += warning
                log.atWarning().log("%s", warning)
            }.copy(warnings = warnings)
            cached = Triple(config, global, profile)
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
