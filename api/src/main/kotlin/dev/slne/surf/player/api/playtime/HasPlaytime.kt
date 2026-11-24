package dev.slne.surf.player.api.playtime

import java.util.*

/** Playtime, sessions, streaks and AFK state of a single player. See [SurfPlaytimeApi]. */
interface HasPlaytime {
    val uuid: UUID
}
