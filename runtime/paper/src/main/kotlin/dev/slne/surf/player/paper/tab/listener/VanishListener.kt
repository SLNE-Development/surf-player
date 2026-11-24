package dev.slne.surf.player.paper.tab.listener

import dev.slne.surf.api.core.event.SurfEventHandler
import dev.slne.surf.player.api.vanish.event.PlayerReappearEvent
import dev.slne.surf.player.api.vanish.event.PlayerVanishEvent
import dev.slne.surf.player.core.client.tab.service.TablistService

object VanishListener {
    @SurfEventHandler
    fun onVanish(event: PlayerVanishEvent) {
        TablistService.updateEntry(event.playerUuid)
    }

    @SurfEventHandler
    fun onReappear(event: PlayerReappearEvent) {
        TablistService.updateEntry(event.playerUuid)
    }
}
