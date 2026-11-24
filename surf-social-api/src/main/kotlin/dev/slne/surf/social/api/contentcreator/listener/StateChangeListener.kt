package dev.slne.surf.social.api.contentcreator.listener

import dev.slne.surf.social.api.contentcreator.ContentCreatorPlatform
import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import java.util.UUID

interface StateChangeListener {

    /**
     * Called when the state of a [ContentCreatorPlatform] has changed.
     *
     * @param playerUuid the minecraft uuid of the affected content creator
     * @param contentCreatorPlatform the content creator plattform
     * @param newState the new state
     */
    fun onStateChanged(playerUuid: UUID, contentCreatorPlatform: ContentCreatorPlatform, newState: PlatformState)

}