package dev.slne.surf.social.paper.minimap

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.social.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.social.core.client.minimap.service.MapSyncService
import dev.slne.surf.social.paper.minimap.command.companionCommand
import dev.slne.surf.social.paper.minimap.listener.CompanionListener
import dev.slne.surf.social.paper.minimap.listener.PluginChannelListener
import dev.slne.surf.social.paper.minimap.platform.PaperCompanionPlayer
import dev.slne.surf.social.paper.minimap.tracker.PaperTracker
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

val isVanishHook get() = pluginManager.isPluginEnabled("surf-vanish-paper")
val isFriendsHook get() = pluginManager.isPluginEnabled("surf-friends-paper")
val isClanHook get() = pluginManager.isPluginEnabled("surf-clan-paper")

class PaperMain : SuspendingJavaPlugin() {

    override fun onEnable() {
        CompanionConfigs.init()
        PluginChannelListener.register()
        CompanionListener.register()
        PaperTracker.configure()
        companionCommand()
        syncOnlinePlayers()
    }

    override fun onDisable() {
        PluginChannelListener.unregister()
    }

    // Players already online when the plugin loads late, such as after a plugin reload.
    private fun syncOnlinePlayers() {
        for (player in server.onlinePlayers) {
            player.scheduler.run(this, {
                MapSyncService.sync(PaperCompanionPlayer(player))
                PaperTracker.reset(player)
            }, null)
            PaperTracker.track(player)
        }
    }
}
