package dev.slne.surf.player.core.client.contentcreator

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.contentcreator.ContentCreatorPlatform
import dev.slne.surf.player.api.contentcreator.listener.StateChangeListener
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

object ContentCreatorInstance {

    private val log = logger()

    private val stateChangeListeners = CopyOnWriteArraySet<StateChangeListener>()

    fun registerStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.add(listener)
    }

    fun callOnStateChangeListener(
        playerUuid: UUID,
        contentCreatorPlatform: ContentCreatorPlatform,
        newState: PlatformState
    ) {
        for (listener in stateChangeListeners) {
            try {
                listener.onStateChanged(playerUuid, contentCreatorPlatform, newState)
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log(
                        "State change listener ${listener.javaClass.name} threw while handling " +
                                "$newState for $playerUuid."
                    )
            }
        }
    }
}
