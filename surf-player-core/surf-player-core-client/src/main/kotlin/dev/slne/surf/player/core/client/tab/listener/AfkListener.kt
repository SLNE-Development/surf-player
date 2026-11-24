package dev.slne.surf.player.core.client.tab.listener

import dev.slne.surf.api.core.event.SurfEventHandler
import dev.slne.surf.player.api.playtime.event.AfkStateChangeEvent
import dev.slne.surf.player.core.client.tab.service.TablistService

/**
 * Keeps the afk tag of a player in the tab list in sync with their afk state.
 */
object AfkListener {
    @SurfEventHandler
    fun onAfkStateChange(event: AfkStateChangeEvent) {
        TablistService.updateEntry(event.playerUuid)
    }
}
