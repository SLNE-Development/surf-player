package dev.slne.surf.player.api.playtime.event

import dev.slne.surf.api.core.event.SurfSyncEvent
import java.util.*

/**
 * Fired on the server the player is on after the afk state of the player identified by
 * [playerUuid] changed to [isAfk].
 */
class AfkStateChangeEvent(val playerUuid: UUID, val isAfk: Boolean) : SurfSyncEvent()
