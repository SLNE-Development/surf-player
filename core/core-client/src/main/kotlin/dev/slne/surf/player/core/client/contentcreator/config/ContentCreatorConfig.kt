package dev.slne.surf.player.core.client.contentcreator.config

import dev.slne.surf.api.core.config.createSpongeYmlConfig
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.player.core.client.contentcreator.platform.ContentCreatorPlatform
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ContentCreatorConfig(
    val liveTag: String = "<red>●</red>",

    val liveStateRefreshSeconds: Long = 60,

    val twitch: TwitchConfig = TwitchConfig(),
)

val config by lazy {
    surfConfigApi.createSpongeYmlConfig<ContentCreatorConfig>(
        ContentCreatorPlatform.dataPath,
        "config.yml"
    )
}

/**
 * Loads the plugin configuration from disk and returns it.
 */
fun loadContentCreatorConfig(): ContentCreatorConfig = config
