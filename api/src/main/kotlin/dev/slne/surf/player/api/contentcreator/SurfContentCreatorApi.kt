package dev.slne.surf.player.api.contentcreator

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.contentcreator.listener.StateChangeListener
import net.kyori.adventure.text.Component
import java.util.*

private val instance = requiredService<SurfContentCreatorApi>()

interface SurfContentCreatorApi {

    /**
     * Registers a new [StateChangeListener] to listen for state changes.
     *
     * @param listener listener
     */
    fun registerStateChangeListener(listener: StateChangeListener)

    /**
     * Returns the content creator behind [playerUuid], or `null` if that player is not on this
     * server or has no linked streaming platform.
     */
    fun contentCreator(playerUuid: UUID): ContentCreator?

    /**
     * Whether the content creator behind [playerUuid] is currently live on any platform.
     */
    fun isLive(playerUuid: UUID): Boolean

    /**
     * Renders the live tag of [playerUuid], or an empty component if that player is not live.
     *
     * @param space whether the tag is prefixed with a space
     */
    fun liveTag(playerUuid: UUID, space: Boolean = true): Component

    companion object : SurfContentCreatorApi by instance {
        val INSTANCE: SurfContentCreatorApi get() = instance
    }
}
