package dev.slne.surf.player.core.client.queue.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.nio.file.Path

@ConfigSerializable
data class SurfQueueConfig(
    val maxTransfersPerSecond: Int = 20,
) {
    /**
     * Owns the `config.yml` of the queue feature. [init] must be called once, by the platform
     * feature, before the config is read.
     */
    companion object {
        private lateinit var configManager: SpongeConfigManager<SurfQueueConfig>

        fun init(dataPath: Path) {
            surfConfigApi.createSpongeYmlConfig(SurfQueueConfig::class.java, dataPath, "config.yml")
            configManager = surfConfigApi.getSpongeConfigManagerForConfig(SurfQueueConfig::class.java)
            configManager.reloadFromFile()
        }

        fun getConfig(): SurfQueueConfig = configManager.config

        /** Reads the `config.yml` again; used by `/surfplayer queue reload`. */
        fun reload() {
            configManager.reloadFromFile()
        }
    }
}
