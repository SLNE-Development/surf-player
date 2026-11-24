package dev.slne.surf.player.api.vanish.event

import dev.slne.surf.api.core.event.SurfSyncEvent
import java.util.*

/**
 * Fired after the player identified by [playerUuid] has vanished.
 */
class PlayerVanishEvent(val playerUuid: UUID) : SurfSyncEvent()
