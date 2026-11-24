package dev.slne.surf.player.api.vanish

import java.util.*

/** The vanish state of a single player. See [SurfVanishApi]. */
interface VanishCapability {
    val uuid: UUID

    /** Whether this player is currently vanished anywhere in the network. */
    val isVanished: Boolean get() = SurfVanishApi.isVanished(uuid)
}
