package dev.slne.surf.player.api.playtime.event

import dev.slne.surf.api.core.event.SurfSyncEvent
import java.util.*

/**
 * Fired on the server the player is on after the afk state of the player identified by
 * [playerUuid] changed to [isAfk].
 *
 * On Paper it is fired on the global region thread (as surf-playtime did), so listeners may touch
 * global server state but must schedule work on the player's entity scheduler to touch the player.
 * On Minestom it is fired directly from the player's tick.
 */
class AfkStateChangeEvent(val playerUuid: UUID, val isAfk: Boolean) : SurfSyncEvent()
