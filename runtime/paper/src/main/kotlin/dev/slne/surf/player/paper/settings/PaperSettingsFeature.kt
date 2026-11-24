package dev.slne.surf.player.paper.settings

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.player.core.client.settings.loadSettings
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.settings.command.settingsCommand
import dev.slne.surf.player.paper.settings.command.settingsAdminActions
import dev.slne.surf.player.paper.settings.listener.PlayerConnectionListener
import dev.slne.surf.player.paper.settings.menu.settingsCategoryView
import dev.slne.surf.player.paper.settings.menu.settingsView

/**
 * Player settings (formerly the `surf-settings-paper` plugin). Loaded first, since the other
 * features read the settings of their players.
 */
object PaperSettingsFeature : PaperPlayerFeature {
    override val name = "settings"

    override suspend fun onLoad() {
        settingsView.register()
        settingsCategoryView.register()
    }

    override suspend fun onEnable() {
        loadSettings()

        PlayerConnectionListener.register()
        settingsCommand()
    }

    override fun adminActions() = settingsAdminActions()
}
