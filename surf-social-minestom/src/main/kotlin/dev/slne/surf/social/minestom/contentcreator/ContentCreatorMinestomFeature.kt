package dev.slne.surf.social.minestom.contentcreator

import dev.slne.surf.social.core.client.contentcreator.ContentCreatorLifecycle
import dev.slne.surf.social.core.client.contentcreator.config.loadContentCreatorConfig
import dev.slne.surf.social.minestom.MinestomSocialFeature
import dev.slne.surf.social.minestom.SocialMinestomPlugin
import dev.slne.surf.social.minestom.contentcreator.command.contentCreatorCommand
import dev.slne.surf.social.minestom.contentcreator.listener.PlayerConnectionListener

/**
 * The former surf-content-creator Minestom plugin.
 */
object ContentCreatorMinestomFeature : MinestomSocialFeature {
    override val id = "contentcreator"

    val dataDirectory get() = SocialMinestomPlugin.featureDataDirectory(this)

    override suspend fun onLoad() {
        loadContentCreatorConfig()

        ContentCreatorLifecycle.enable()
    }

    override suspend fun onEnable() {
        contentCreatorCommand()

        PlayerConnectionListener.register(SocialMinestomPlugin.eventNode)
    }

    override suspend fun onDisable() {
        ContentCreatorLifecycle.disable()
    }
}
