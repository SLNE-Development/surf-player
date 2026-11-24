package dev.slne.surf.player.core.client.tab.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.player.core.client.tab.platform.TabPlatform

/**
 * The tablist's `config.yml`, loaded from the data directory of the running platform.
 */
object TablistConfigFile : SpongeYmlConfigClass<TablistConfig>(
    TablistConfig::class.java,
    TabPlatform.dataPath,
    "config.yml"
)

val tablistConfig: TablistConfig get() = TablistConfigFile.getConfig()
