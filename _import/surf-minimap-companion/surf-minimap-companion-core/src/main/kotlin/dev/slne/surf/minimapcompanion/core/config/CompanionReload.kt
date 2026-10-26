package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.minimapcompanion.core.xaero.XaeroServerProfile
import kotlin.time.Duration
import kotlin.time.measureTimedValue

/**
 * Reloads `config.yml`. The platforms then sync every player again, since the reloaded config
 * can change the level IDs, the rules and the enforced options.
 */
object CompanionReload {

    data class Result(val config: MinimapCompanionConfig, val profile: XaeroServerProfile, val duration: Duration)

    /**
     * Loads the config with [load] and checks its Xaero options, which logs their warnings.
     */
    fun reload(load: () -> MinimapCompanionConfig = CompanionConfig::reloadFromFile): Result {
        val (result, duration) = measureTimedValue {
            val config = load()
            config to XaeroServerProfile.of(config)
        }
        return Result(result.first, result.second, duration)
    }
}
