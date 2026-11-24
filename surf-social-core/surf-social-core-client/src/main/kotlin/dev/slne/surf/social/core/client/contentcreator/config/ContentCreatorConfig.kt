package dev.slne.surf.social.core.client.contentcreator.config

import dev.slne.surf.api.core.config.createSpongeYmlConfig
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.social.core.client.contentcreator.coreApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ContentCreatorConfig(
    val liveTag: String = "<red>●</red>",

    val liveStateRefreshSeconds: Long = 60,

    val twitch: Twitch = Twitch(),
) {
    @ConfigSerializable
    data class Twitch(
        val clientId: String = "",
        val clientSecret: String = ""
    )
}

val config by lazy {
    surfConfigApi.createSpongeYmlConfig<ContentCreatorConfig>(
        coreApi.dataPath,
        "config.yml"
    )
}

/**
 * Loads the plugin configuration from disk and returns it.
 */
fun loadContentCreatorConfig(): ContentCreatorConfig = config