package dev.slne.surf.player.core.client.minimap.voxelmap

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.core.client.minimap.config.VoxelMapConfig
import dev.slne.surf.player.core.client.minimap.global.GlobalSettings

/**
 * What this server enforces on VoxelMap: `Boolean` values for the keys in [BOOLEANS] and a `String` for
 * [TELEPORT_COMMAND]. [warnings] lists the configured options that are not sent.
 */
data class VoxelMapProfile(
    val values: Map<String, Any>,
    val warnings: List<String> = emptyList(),
) {
    companion object {
        private val log = logger()

        /**
         * VoxelMap's switches, in the order its server part sends them. VoxelMap allows all of them by default.
         */
        val BOOLEANS = listOf(
            "radarAllowed",
            "radarMobsAllowed",
            "radarPlayersAllowed",
            "cavesAllowed",
            "minimapAllowed",
            "worldmapAllowed",
            "waypointsAllowed",
            "deathWaypointAllowed",
        )

        const val TELEPORT_COMMAND = "teleportCommand"

        // serverIdentity would move the client's map data; the sub-world comes from the world ID instead of worldName.
        private val COMPANION_OWNED = setOf("serverIdentity", "worldName")

        @Volatile
        private var cached: Triple<VoxelMapConfig, GlobalSettings, VoxelMapProfile>? = null

        /**
         * Returns the profile [config] describes, with the entries [global] sets applied. Each pair of instances is
         * parsed once, so its warnings and the options the global settings override are logged once.
         */
        fun of(config: VoxelMapConfig, global: GlobalSettings = GlobalSettings.NONE): VoxelMapProfile {
            cached?.let { (cachedConfig, cachedGlobal, profile) ->
                if (cachedConfig === config && cachedGlobal === global) return profile
            }

            val options = VoxelMapGlobalSettings.apply(config.options.mapValues { it.value.raw() }, global) { hint ->
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
         * Reads the options in [raw] as the config holds them. VoxelMap disconnects on a value of the wrong type
         * and ignores keys it does not know, so each value that cannot be sent is skipped and reported through
         * [warn]; a `null` value enforces nothing.
         */
        fun parse(raw: Map<String, Any?>, warn: (String) -> Unit): VoxelMapProfile {
            val values = LinkedHashMap<String, Any>()

            for ((id, value) in raw) {
                if (value == null) continue

                when (id) {
                    in COMPANION_OWNED -> warn("Skipping VoxelMap option $id: the companion sets it")
                    in BOOLEANS -> when (value) {
                        is Boolean -> values[id] = value
                        else -> warn("Skipping VoxelMap option $id: $value is not true or false")
                    }

                    TELEPORT_COMMAND -> {
                        // VoxelMap sends the command without a slash.
                        val command = (value as? String)?.trim()?.removePrefix("/")
                        when {
                            command == null -> warn("Skipping VoxelMap option $id: $value is not a command")
                            command.isBlank() -> warn("Skipping VoxelMap option $id: the command is empty")
                            else -> values[id] = command
                        }
                    }

                    else -> warn("Skipping unknown VoxelMap option $id: VoxelMap ignores keys it does not know")
                }
            }

            return VoxelMapProfile(values)
        }
    }
}
