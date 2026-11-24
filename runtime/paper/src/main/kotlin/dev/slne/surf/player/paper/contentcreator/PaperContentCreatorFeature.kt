package dev.slne.surf.player.paper.contentcreator

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.contentcreator.ContentCreatorLifecycle
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.contentcreator.command.surfContentCreatorCommand
import dev.slne.surf.player.paper.contentcreator.listener.PlayerConnectionListener

/**
 * The former surf-content-creator Paper plugin.
 */
object PaperContentCreatorFeature : PaperPlayerFeature {
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
