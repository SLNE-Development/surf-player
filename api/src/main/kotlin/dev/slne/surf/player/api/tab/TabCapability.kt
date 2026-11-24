package dev.slne.surf.player.api.tab

import java.util.*

/** The tab list entry of a single player. See [SurfTabApi]. */
interface TabCapability {
    val uuid: UUID

    /** Re-renders this player's tab entry on this server. See [SurfTabApi.updateTabEntry]. */
    fun updateTabEntry() = SurfTabApi.updateTabEntry(uuid)

    /** Re-renders this player's tab entry on every server. See [SurfTabApi.broadcastTabEntryUpdate]. */
    fun broadcastTabEntryUpdate() = SurfTabApi.broadcastTabEntryUpdate(uuid)
}
