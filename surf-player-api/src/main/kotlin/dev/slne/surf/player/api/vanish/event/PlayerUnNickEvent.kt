package dev.slne.surf.player.api.vanish.event

import dev.slne.surf.api.core.event.SurfSyncEvent
import java.util.*

/**
 * Fired after the nickname of the player identified by [playerUuid] has been removed.
 */
class PlayerUnNickEvent(val playerUuid: UUID) : SurfSyncEvent()
