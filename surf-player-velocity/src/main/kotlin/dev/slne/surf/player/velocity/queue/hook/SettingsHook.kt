package dev.slne.surf.player.velocity.queue.hook

import dev.slne.surf.player.velocity.queue.hook.integration.SurfSettingsIntegration
import dev.slne.surf.player.velocity.queue.plugin
import java.util.UUID

object SettingsHook {
    private const val SETTINGS_PLUGIN_ID = "surf-settings-velocity"

    val available: Boolean
        get() = plugin.proxy.pluginManager
            .getPlugin(SETTINGS_PLUGIN_ID)
            .isPresent

    suspend fun hasAutoQueueAfterReconnectEnabled(playerUuid: UUID): Boolean {
        return !available || SurfSettingsIntegration.hasAutoQueueAfterReconnectEnabled(playerUuid)
    }
}