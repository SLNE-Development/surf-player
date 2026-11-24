package dev.slne.surf.player.api.contentcreator.listener

import dev.slne.surf.player.api.contentcreator.StreamingPlatform
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import java.util.UUID

fun interface StateChangeListener {

    /**
     * Called when the state of a [StreamingPlatform] of a content creator has changed.
     *
     * @param playerUuid the minecraft uuid of the affected content creator
     * @param streamingPlatform the streaming platform whose state changed
     * @param newState the new state
     */
    fun onStateChanged(playerUuid: UUID, streamingPlatform: StreamingPlatform, newState: PlatformState)

}
