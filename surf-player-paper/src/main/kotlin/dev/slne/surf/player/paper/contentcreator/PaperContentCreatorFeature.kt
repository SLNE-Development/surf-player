package dev.slne.surf.social.paper.contentcreator

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.social.core.client.contentcreator.ContentCreatorLifecycle
import dev.slne.surf.social.paper.PaperSocialFeature
import dev.slne.surf.social.paper.contentcreator.command.surfContentCreatorCommand
import dev.slne.surf.social.paper.contentcreator.listener.PlayerConnectionListener

/**
 * The former surf-content-creator Paper plugin.
 */
object PaperContentCreatorFeature : PaperSocialFeature {
    override val name = "contentcreator"

    override suspend fun onEnable() {
        ContentCreatorLifecycle.enable()

        PlayerConnectionListener.register()

        surfContentCreatorCommand()
    }

    override suspend fun onDisable() {
        ContentCreatorLifecycle.disable()
    }
}
