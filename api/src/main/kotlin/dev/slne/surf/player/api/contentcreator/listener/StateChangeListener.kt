package dev.slne.surf.player.api.contentcreator.listener

import dev.slne.surf.player.api.contentcreator.ContentCreatorPlatform
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import java.util.UUID

fun interface StateChangeListener {

    /**
     * Called when the state of a [ContentCreatorPlatform] has changed.
     *
     * @param playerUuid the minecraft uuid of the affected content creator
     * @param contentCreatorPlatform the content creator plattform
     * @param newState the new state
     */
    fun onStateChanged(playerUuid: UUID, contentCreatorPlatform: ContentCreatorPlatform, newState: PlatformState)

}