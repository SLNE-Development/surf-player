package dev.slne.surf.player.core.client.contentcreator.config

import dev.slne.surf.api.core.config.createSpongeYmlConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.player.core.client.contentcreator.platform.ContentCreatorPlatform
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ContentCreatorConfig(
    val liveTag: String = "<red>●</red>",

    val liveStateRefreshSeconds: Long = 60,

    val twitch: TwitchConfig = TwitchConfig(),
)

private val manager by lazy {
    surfConfigApi.createSpongeYmlConfigManager<ContentCreatorConfig>(
        ContentCreatorPlatform.dataPath,
        "config.yml"
    )
}

/** The current configuration; read it on every use so that a reload takes effect. */
val config: ContentCreatorConfig get() = manager.config

/**
 * Loads the plugin configuration from disk and returns it.
 */
fun loadContentCreatorConfig(): ContentCreatorConfig = config

/**
 * Reads the configuration from disk again (`/surfplayer contentcreator reload`). The Twitch client
 * keeps the credentials it was started with until the next restart.
 */
fun reloadContentCreatorConfig(): ContentCreatorConfig = manager.reloadFromFile()
