package dev.slne.surf.player.core.client.clan

import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.core.common.clan.config.ClanConfig
import java.nio.file.Path

/**
 * Lifecycle of the clan feature on a client platform, driven by the platform's clan feature.
 */
object ClanClient {

    /**
     * Loads the clan config from [dataPath] and creates the clan caches. Must run before the
     * shared Redis connection is established.
     */
    fun load(dataPath: Path) {
        ClanConfig.init(dataPath)
        ClientClanService.init()
    }
}
