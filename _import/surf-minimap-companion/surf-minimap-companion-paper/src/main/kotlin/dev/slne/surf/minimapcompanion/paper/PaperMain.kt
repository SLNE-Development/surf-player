package dev.slne.surf.minimapcompanion.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
import dev.slne.surf.minimapcompanion.core.service.XaeroSyncService
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.paper.listener.CompanionListener
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.tracker.PaperTracker
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {

    override fun onEnable() {
        CompanionConfig.init()

        for (channel in XaeroChannels.ALL) {
            server.messenger.registerOutgoingPluginChannel(this, channel)
        }

        CompanionListener.register()
        PaperTracker.start()

        for (player in server.onlinePlayers) {
            player.scheduler.run(this, {
                XaeroSyncService.sync(PaperCompanionPlayer(player))
                PaperTracker.reset(player)
            }, null)
            PaperTracker.track(player)
        }
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this)
    }
}
