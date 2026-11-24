package dev.slne.surf.player.paper.minimap

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.service.MapSyncService
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.minimap.command.companionCommand
import dev.slne.surf.player.paper.minimap.listener.CompanionListener
import dev.slne.surf.player.paper.minimap.listener.PluginChannelListener
import dev.slne.surf.player.paper.minimap.platform.PaperMinimapPlayer
import dev.slne.surf.player.paper.minimap.tracker.PaperTracker
import dev.slne.surf.player.paper.plugin
import org.bukkit.Bukkit

/**
 * The former surf-minimap-companion Paper plugin.
 */
object PaperMinimapFeature : PaperPlayerFeature {
    override val name = "minimap"

    override suspend fun onEnable() {
        CompanionConfigs.init()
        PluginChannelListener.register()
        CompanionListener.register()
        PaperTracker.configure()
        companionCommand()
        syncOnlinePlayers()
    }

    override suspend fun onDisable() {
        PluginChannelListener.unregister()
    }

    // Players already online when the plugin loads late, such as after a plugin reload.
    private fun syncOnlinePlayers() {
        for (player in Bukkit.getOnlinePlayers()) {
            player.scheduler.run(plugin, {
                MapSyncService.sync(PaperMinimapPlayer(player))
                PaperTracker.reset(player)
            }, null)
            PaperTracker.track(player)
        }
    }
}
