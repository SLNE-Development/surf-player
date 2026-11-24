package dev.slne.surf.player.api.profile

import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val instance = requiredService<SurfProfileApi>()

/**
 * The profile menu of players (Paper and Minestom).
 */
interface SurfProfileApi {
    /** Opens the own-profile menu for the player identified by [playerUuid], if they are online here. */
    fun openOwnProfileMenu(playerUuid: UUID)

    companion object : SurfProfileApi by instance {
        val INSTANCE: SurfProfileApi get() = instance
    }
}
