package dev.slne.surf.player.velocity.settings

import dev.slne.surf.player.core.client.settings.loadSettings
import dev.slne.surf.player.velocity.VelocityPlayerFeature
import dev.slne.surf.player.velocity.plugin
import dev.slne.surf.player.velocity.settings.listener.PlayerConnectionListener

/**
 * Player settings (formerly the `surf-settings-velocity` plugin). Loaded first, since the other
 * features read the settings of their players.
 */
object SettingsVelocityFeature : VelocityPlayerFeature {
    override val id = "settings"

    override suspend fun onEnable() {
        loadSettings()

        plugin.eventManager.register(plugin, PlayerConnectionListener)
    }
}
