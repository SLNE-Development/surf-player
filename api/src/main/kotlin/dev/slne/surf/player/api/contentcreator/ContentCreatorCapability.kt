package dev.slne.surf.player.api.contentcreator

import net.kyori.adventure.text.Component
import java.util.*

/** The content creator state of a single player. See [SurfContentCreatorApi]. */
interface ContentCreatorCapability {
    val uuid: UUID

    /** This player as a content creator, or `null` if they have no linked streaming platform. */
    fun contentCreator(): ContentCreator? = SurfContentCreatorApi.contentCreator(uuid)

    /** Whether this player is currently live on any platform. */
    val isLive: Boolean get() = SurfContentCreatorApi.isLive(uuid)

    /** The live tag of this player, or an empty component if they are not live. */
    fun liveTag(space: Boolean = true): Component = SurfContentCreatorApi.liveTag(uuid, space)
}
