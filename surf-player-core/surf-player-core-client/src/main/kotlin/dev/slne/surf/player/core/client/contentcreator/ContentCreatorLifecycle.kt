package dev.slne.surf.social.core.client.contentcreator

import dev.slne.surf.social.core.client.contentcreator.client.ContentClientManager
import dev.slne.surf.social.core.client.contentcreator.platform.platformBridge
import dev.slne.surf.social.core.client.contentcreator.service.ContentCreatorService

/**
 * Starts and stops the platform-neutral part of the content creator plugin.
 */
object ContentCreatorLifecycle {

    /**
     * Connects every content client and starts listening for the streams of all online players.
     */
    suspend fun enable() {
        ContentClientManager.startAll(platformBridge.pluginScope)
        ContentClientManager.enableStreamEventListener(platformBridge.onlinePlayerUuids())
    }

    /**
     * Closes every content client.
     */
    fun disable() {
        ContentClientManager.closeAll()
        ContentCreatorService.clear()
    }
}
