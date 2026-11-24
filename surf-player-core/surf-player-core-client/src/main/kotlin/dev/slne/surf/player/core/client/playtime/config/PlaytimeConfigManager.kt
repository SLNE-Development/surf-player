package dev.slne.surf.player.core.client.playtime.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import java.nio.file.Path

/**
 * Owns the `config.yml` of the playtime feature. [load] must be called once, by the platform
 * feature, before the config is read.
 */
object PlaytimeConfigManager {
    private lateinit var configManager: SpongeConfigManager<PlaytimeConfig>

    val config: PlaytimeConfig get() = configManager.config

    fun load(dataPath: Path) {
        surfConfigApi.createSpongeYmlConfig(PlaytimeConfig::class.java, dataPath, "config.yml")
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(PlaytimeConfig::class.java)
        reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }
}

val playtimeConfig get() = PlaytimeConfigManager.config
