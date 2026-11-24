package dev.slne.surf.player.paper.vanish.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.nio.file.Path

@ConfigSerializable
data class VanishConfig(
    val spoofConnectionMessages: Boolean = true
)

private lateinit var configManager: SpongeConfigManager<VanishConfig>

val vanishConfig: VanishConfig get() = configManager.config

fun initVanishConfig(dataPath: Path) {
    surfConfigApi.createSpongeYmlConfig(VanishConfig::class.java, dataPath, "config.yml")
    configManager = surfConfigApi.getSpongeConfigManagerForConfig(VanishConfig::class.java)
    reloadVanishConfig()
}

fun reloadVanishConfig() {
    configManager.reloadFromFile()
}
