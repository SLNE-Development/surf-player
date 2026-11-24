package dev.slne.surf.player.api.profile

import java.util.*

/** The profile of a single player. See [SurfProfileApi]. */
interface ProfileCapability {
    val uuid: UUID

    /** Opens this player's own-profile menu, if they are online here. */
    fun openProfileMenu() = SurfProfileApi.openOwnProfileMenu(uuid)
}
