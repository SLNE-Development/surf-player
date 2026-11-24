package dev.slne.surf.player.core.client.playtime.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.player.core.client.playtime.ClientPlaytimeInstance
import dev.slne.surf.player.core.common.playtime.config.PlaytimeConfig

class PlaytimeConfigManager {
    private val configManager: SpongeConfigManager<PlaytimeConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            PlaytimeConfig::class.java,
            ClientPlaytimeInstance.dataPath,
            "config.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(
            PlaytimeConfig::class.java
        )
        reload()
    }

    fun edit(actions: PlaytimeConfig.() -> Unit) {
        configManager.config = configManager.config.apply { actions() }
        configManager.save()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}

val playtimeConfigManager = PlaytimeConfigManager()
val playtimeConfig get() = playtimeConfigManager.config
