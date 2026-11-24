package dev.slne.surf.social.paper.tab.listener

import dev.slne.surf.social.paper.playtime.api.event.AfkStateChangeEvent
import dev.slne.surf.social.core.client.tab.service.TablistService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object PlaytimeListener : Listener {
    @EventHandler
    fun onAfkChange(event: AfkStateChangeEvent) = TablistService.updateEntry(event.playerUuid)
}
