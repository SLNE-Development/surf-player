package dev.slne.surf.social.core.client.tab.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.social.core.client.tab.platform.tabPlatform

class TablistConfigProvider {
    private val configManager: SpongeConfigManager<TablistConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            TablistConfig::class.java,
            tabPlatform.dataPath,
            "config.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(TablistConfig::class.java)

        this.reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}
