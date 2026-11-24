package dev.slne.surf.player.core.client.settings.redis

import dev.slne.surf.player.core.client.settings.service.SettingsService
import dev.slne.surf.redis.event.OnRedisEvent

/**
 * Keeps the per-server settings caches of every platform in sync with changes made elsewhere.
 * Subscribed by [dev.slne.surf.player.core.client.SurfPlayerClient.load].
 */
object SettingsRedisListener {
    @OnRedisEvent
    fun onPlayerSettingChanged(event: PlayerSettingChangedRedisEvent) {
        SettingsService.applySavedSetting(event.playerUuid, event.settingName, event.value)
    }
}
