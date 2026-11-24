package dev.slne.surf.player.paper.contentcreator.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.client.contentcreator.client.ContentClientManager
import dev.slne.surf.player.core.client.contentcreator.service.ContentCreatorService
import dev.slne.surf.player.paper.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerConnectionListener : Listener {
    @EventHandler
    fun onConnect(event: PlayerJoinEvent) {
        val playerUuid = event.player.uniqueId

        ContentCreatorService.beginSession(playerUuid)

        plugin.launch {
            ContentCreatorService.cacheCreator(playerUuid)
            ContentClientManager.enableStreamEventListener(playerUuid)
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        val creator = ContentCreatorService.endSession(event.player.uniqueId) ?: return

        plugin.launch {
            ContentClientManager.disableStreamEventListener(creator)
        }
    }
}
