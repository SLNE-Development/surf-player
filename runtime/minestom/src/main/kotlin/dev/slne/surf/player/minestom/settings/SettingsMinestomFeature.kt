package dev.slne.surf.player.minestom.settings

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.inventory.framework.register
import dev.slne.surf.player.core.client.settings.loadSettings
import dev.slne.surf.player.core.client.settings.service.SettingsService
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.settings.command.settingsCommand
import dev.slne.surf.player.minestom.settings.command.settingsAdminActions
import dev.slne.surf.player.minestom.settings.menu.settingsCategoryView
import dev.slne.surf.player.minestom.settings.menu.settingsView
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

/**
 * Player settings (formerly the `surf-settings-minestom` plugin). Loaded first, since the other
 * features read the settings of their players.
 */
object SettingsMinestomFeature : MinestomPlayerFeature {
    override val id = "settings"

    override suspend fun onLoad() {
        settingsView.register()
        settingsCategoryView.register()
    }

    override suspend fun onEnable() {
        loadSettings()

        settingsCommand()
    }

    override fun adminActions() = settingsAdminActions()

    override fun onPlayerJoin(player: Player) {
        minestomAsyncScope.launch {
            SettingsService.cacheJoinedPlayerSettings(player.uuid) { player.isOnline }
        }
    }

    override fun onPlayerQuit(player: Player) {
        SettingsService.invalidatePlayerSettingsCache(player.uuid)
    }
}
