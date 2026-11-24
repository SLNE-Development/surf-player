package dev.slne.surf.player.core.client.minimap.global

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.core.client.minimap.config.GlobalSettingsConfig

/**
 * The features `global-settings` switches for every mod: `true` allows a feature and `false` forbids it, whatever
 * the mod files say. Features left at `per-mod` are not in [overrides]. [warnings] lists the values that were not
 * understood and act as `per-mod`.
 */
data class GlobalSettings(
    val overrides: Map<GlobalFeature, Boolean>,
    val warnings: List<String> = emptyList(),
) {
    operator fun get(feature: GlobalFeature): Boolean? = overrides[feature]

    companion object {
        private val log = logger()

        private const val PER_MOD = "per-mod"

        val NONE = GlobalSettings(emptyMap())

        @Volatile
        private var cached: Pair<GlobalSettingsConfig, GlobalSettings>? = null

        /**
         * Returns the settings [config] describes. Each config instance is parsed once, so its warnings are logged once.
         */
        fun of(config: GlobalSettingsConfig): GlobalSettings {
            cached?.let { (cachedConfig, settings) -> if (cachedConfig === config) return settings }

            val warnings = mutableListOf<String>()
            val settings = parse(config.entries().mapValues { it.value.raw() }) { warning ->
                warnings += warning
                log.atWarning().log("%s", warning)
            }.copy(warnings = warnings)
            cached = config to settings
            return settings
        }

        /**
         * Reads the entries in [raw] as the config holds them. `true` and `false` count as written or quoted;
         * `per-mod` and a missing value leave the feature to the mod files. Anything else is reported through [warn]
         * and acts as `per-mod`.
         */
        fun parse(raw: Map<GlobalFeature, Any?>, warn: (String) -> Unit): GlobalSettings {
            val overrides = LinkedHashMap<GlobalFeature, Boolean>()

            for ((feature, value) in raw) {
                when {
                    value == null -> continue
                    value is Boolean -> overrides[feature] = value
                    value is String && value.equals(PER_MOD, ignoreCase = true) -> continue
                    value is String && value.equals("true", ignoreCase = true) -> overrides[feature] = true
                    value is String && value.equals("false", ignoreCase = true) -> overrides[feature] = false
                    else -> warn("Unknown value \"$value\" for global-settings.${feature.key}, using $PER_MOD")
                }
            }

            return GlobalSettings(overrides)
        }
    }
}
