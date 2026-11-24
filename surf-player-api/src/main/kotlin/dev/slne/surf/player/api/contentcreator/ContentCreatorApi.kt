package dev.slne.surf.player.api.contentcreator

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.contentcreator.listener.StateChangeListener
import net.kyori.adventure.text.Component
import java.util.*

private val instance = requiredService<ContentCreatorApi>()

interface ContentCreatorApi {

    /**
     * Registers a new [StateChangeListener] to listen for state changes.
     *
     * @param listener listener
     */
    fun registerStateChangeListener(listener: StateChangeListener)
    fun renderLiveTag(playerUuid: UUID, space: Boolean = true): Component

    companion object : ContentCreatorApi by instance {
        val INSTANCE get() = instance
    }
}