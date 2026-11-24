package dev.slne.surf.player.minestom.settings

import dev.slne.surf.api.minestom.inventory.framework.register
import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.player.core.client.settings.ClientSettingsInstance
import dev.slne.surf.player.core.client.settings.createDefaultSettings
import dev.slne.surf.player.core.common.settings.service.SettingsService
import dev.slne.surf.player.minestom.settings.command.settingsCommand
import dev.slne.surf.player.minestom.settings.command.surfSettingsCommand
import dev.slne.surf.player.minestom.settings.listener.PlayerConnectionListener
import dev.slne.surf.player.minestom.settings.menu.settingsCategoryView
import dev.slne.surf.player.minestom.settings.menu.settingsView

object SurfSettingsMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        settingsView.register()
        settingsCategoryView.register()

        ClientSettingsInstance.clientLoader.onLoad()

        SettingsService.refreshSettings()
        createDefaultSettings()
    }

    override suspend fun onEnable() {
        ClientSettingsInstance.clientLoader.onEnable()
        settingsCommand()
        surfSettingsCommand()

        PlayerConnectionListener.register(eventNode)
    }

    override suspend fun onDisable() {
        ClientSettingsInstance.clientLoader.onDisable()
    }
}
