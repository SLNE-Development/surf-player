package dev.slne.surf.player.minestom.contentcreator

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.player.core.client.contentcreator.ContentCreatorLifecycle
import dev.slne.surf.player.core.client.contentcreator.client.ContentClientManager
import dev.slne.surf.player.core.client.contentcreator.config.loadContentCreatorConfig
import dev.slne.surf.player.core.client.contentcreator.service.ContentCreatorService
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.command.contentCreatorAdminActions
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

/**
 * The former surf-content-creator Minestom plugin.
 */
object ContentCreatorMinestomFeature : MinestomPlayerFeature {
    override val id = "contentcreator"

    override suspend fun onLoad() {
        loadContentCreatorConfig()

        ContentCreatorLifecycle.enable()
    }

    override fun adminActions() = contentCreatorAdminActions()

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
