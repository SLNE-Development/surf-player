package dev.slne.surf.player.core.common.clan.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.nio.file.Path

@ConfigSerializable
data class ClanConfig(
    val whitelistedTags: List<String> = emptyList() // TODO: save in db
) {
    companion object {
        private lateinit var provider: SpongeYmlConfigClass<ClanConfig>

        /** Loads the clan config from the directory [dataPath]. Must run before [getConfig]. */
        fun init(dataPath: Path) {
            provider = object : SpongeYmlConfigClass<ClanConfig>(
                ClanConfig::class.java,
                dataPath,
                "config.yml"
            ) {}
        }

        fun getConfig(): ClanConfig = provider.getConfig()

        fun reloadFromFile() {
            provider.reloadFromFile()
        }
    }
}
