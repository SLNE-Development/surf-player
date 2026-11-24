package dev.slne.surf.player.paper.settings

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.player.core.client.settings.ClientSettingsInstance
import dev.slne.surf.player.core.client.settings.createDefaultSettings
import dev.slne.surf.player.core.common.settings.service.SettingsService
import dev.slne.surf.player.paper.settings.command.settingsCommand
import dev.slne.surf.player.paper.settings.command.surfSettingsCommand
import dev.slne.surf.player.paper.settings.listener.PlayerConnectionListener
import dev.slne.surf.player.paper.settings.menu.settingsCategoryView
import dev.slne.surf.player.paper.settings.menu.settingsView
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        ClientSettingsInstance.clientLoader.onLoad()

        settingsView.register()
        settingsCategoryView.register()
    }

    override suspend fun onEnableAsync() {
        ClientSettingsInstance.clientLoader.onEnable()
        PlayerConnectionListener.register()

        SettingsService.refreshSettings()

        surfSettingsCommand()
        settingsCommand()

        createDefaultSettings()
    }

    override suspend fun onDisableAsync() {
        ClientSettingsInstance.clientLoader.onDisable()
    }
}
