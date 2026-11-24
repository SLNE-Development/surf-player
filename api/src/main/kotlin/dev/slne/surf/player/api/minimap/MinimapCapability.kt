package dev.slne.surf.player.api.minimap

import java.util.*

/** The minimap integration state of a single player. See [SurfMinimapApi]. */
interface MinimapCapability {
    val uuid: UUID

    /** The level ID last sent to this player's minimap mods, or `null` if they are offline. */
    fun minimapLevelId(): Int? = SurfMinimapApi.minimapLevelId(uuid)
}
