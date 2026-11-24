package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.minimapcompanion.core.config.XaeroConfig
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import dev.slne.surf.minimapcompanion.core.xaero.option.EnforcedOptions
import dev.slne.surf.minimapcompanion.core.xaero.option.MinimapOptions
import dev.slne.surf.minimapcompanion.core.xaero.option.WorldMapOptions

/**
 * What this server enforces on Xaero's mods, like the enforced server profile of a server with the mods installed.
 * [warnings] lists the configured options that are not sent, or sent unchecked.
 */
data class XaeroServerProfile(
    val minimap: EnforcedOptions,
    val worldMap: EnforcedOptions,
    val warnings: List<String> = emptyList(),
) {

    fun minimapRules(): ByteArray {
        val (caveMode, netherCaveMode) = caveRules(minimap, "minimap_cave_mode_allowed", "minimap_cave_mode_allowed_dimensions")
        return XaeroPackets.minimapRules(caveMode, netherCaveMode, radar = minimap.boolean("display_radar") != false)
    }

    fun worldMapRules(): ByteArray {
        val (caveMode, netherCaveMode) = caveRules(worldMap, "cave_mode_allowed", "cave_mode_allowed_dimensions")
        return XaeroPackets.worldMapRules(caveMode, netherCaveMode)
    }

    /**
     * The config channels Xaero's Lib syncs, in the order the mods send them, with what each enforces.
     */
    fun configChannels() = listOf(
        XaeroChannels.LIB to EnforcedOptions.EMPTY,
        XaeroChannels.MINIMAP to minimap,
        XaeroChannels.WORLD_MAP to worldMap,
    )

    // Mirrors how the mods derive their rules packet from the enforced profile.
    private fun caveRules(options: EnforcedOptions, allowedId: String, dimensionsId: String): Pair<Boolean, Boolean> {
        if (options.boolean(allowedId) == false) return false to false

        val dimensions = options.identifiers(dimensionsId).orEmpty()
        fun allows(dimension: String) = dimensions.isEmpty() || dimension in dimensions
        return allows("minecraft:overworld") to allows("minecraft:the_nether")
    }

    companion object {
        private val log = logger()

        @Volatile
        private var cached: Triple<XaeroConfig, GlobalSettings, XaeroServerProfile>? = null

        /**
         * Returns the profile [config] describes, with the entries [global] sets applied. Each pair of instances is
         * parsed once, so its warnings and the options the global settings override are logged once.
         */
        fun of(config: XaeroConfig, global: GlobalSettings = GlobalSettings.NONE): XaeroServerProfile {
            cached?.let { (cachedConfig, cachedGlobal, profile) ->
                if (cachedConfig === config && cachedGlobal === global) return profile
            }

            val (minimap, worldMap) = XaeroGlobalSettings.apply(
                config.minimap.mapValues { it.value.raw() },
                config.worldMap.mapValues { it.value.raw() },
                global,
            ) { hint -> log.atInfo().log("%s", hint) }

            val warnings = mutableListOf<String>()
            val profile = parse(minimap, worldMap) { warning ->
                warnings += warning
                log.atWarning().log("%s", warning)
            }.copy(warnings = warnings)
            cached = Triple(config, global, profile)
            return profile
        }

        fun parse(minimap: Map<String, Any?>, worldMap: Map<String, Any?>, warn: (String) -> Unit) = XaeroServerProfile(
            EnforcedOptions.parse(MinimapOptions, minimap, warn),
            EnforcedOptions.parse(WorldMapOptions, worldMap, warn),
        )
    }
}
