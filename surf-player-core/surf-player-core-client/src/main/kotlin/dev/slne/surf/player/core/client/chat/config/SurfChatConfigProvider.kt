package dev.slne.surf.player.core.client.chat.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.player.core.client.chat.config.configs.SurfChatConfig
import java.nio.file.Path

lateinit var chatConfigProvider: SpongeYmlConfigClass<SurfChatConfig>
    private set

val chatConfig: SurfChatConfig get() = chatConfigProvider.getConfig()

fun initChatConfig(dataPath: Path) {
    chatConfigProvider = object : SpongeYmlConfigClass<SurfChatConfig>(
        SurfChatConfig::class.java,
        dataPath,
        "config.yml"
    ) {}
}
