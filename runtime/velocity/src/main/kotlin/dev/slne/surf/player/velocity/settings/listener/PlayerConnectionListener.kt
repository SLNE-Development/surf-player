package dev.slne.surf.player.velocity.settings.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.core.client.settings.service.SettingsService

object PlayerConnectionListener {
    private val log = logger()

    @Subscribe
    suspend fun onPostLogin(event: PostLoginEvent) {
        val player = event.player

        try {
            SettingsService.cacheJoinedPlayerSettings(player.uniqueId) { player.isActive }
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to cache settings for %s", player.uniqueId)
        }
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        SettingsService.invalidatePlayerSettingsCache(event.player.uniqueId)
    }
}
