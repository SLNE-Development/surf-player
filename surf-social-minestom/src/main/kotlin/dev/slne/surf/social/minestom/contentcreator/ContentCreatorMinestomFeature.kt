package dev.slne.surf.social.minestom.contentcreator

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.social.core.client.contentcreator.ContentCreatorLifecycle
import dev.slne.surf.social.core.client.contentcreator.client.ContentClientManager
import dev.slne.surf.social.core.client.contentcreator.config.loadContentCreatorConfig
import dev.slne.surf.social.core.client.contentcreator.service.ContentCreatorService
import dev.slne.surf.social.minestom.MinestomSocialFeature
import dev.slne.surf.social.minestom.contentcreator.command.contentCreatorCommand
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

/**
 * The former surf-content-creator Minestom plugin.
 */
object ContentCreatorMinestomFeature : MinestomSocialFeature {
    override val id = "contentcreator"

    override suspend fun onLoad() {
        loadContentCreatorConfig()

        ContentCreatorLifecycle.enable()
    }

    override suspend fun onEnable() {
        contentCreatorCommand()
    }

    override suspend fun onDisable() {
        ContentCreatorLifecycle.disable()
    }

    override fun onPlayerJoin(player: Player) {
        val playerUuid = player.uuid

        ContentCreatorService.beginSession(playerUuid)

        minestomAsyncScope.launch {
            ContentCreatorService.cacheCreator(playerUuid)
            ContentClientManager.enableStreamEventListener(playerUuid)
        }
    }

    override fun onPlayerQuit(player: Player) {
        val creator = ContentCreatorService.endSession(player.uuid) ?: return

        minestomAsyncScope.launch {
            ContentClientManager.disableStreamEventListener(creator)
        }
    }
}
