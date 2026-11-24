package dev.slne.surf.player.velocity.queue.hook.integration

import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import java.util.UUID

object SurfSettingsIntegration {
    suspend fun hasAutoQueueAfterReconnectEnabled(playerUuid: UUID): Boolean {
        return SurfSettingsApi.getCachedValueOrLoad(
            playerUuid,
            SettingKeys.AUTO_QUEUE_AFTER_RECONNECT,
        )
    }
}