package dev.slne.surf.minimapcompanion.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapChannels
import dev.slne.surf.minimapcompanion.core.service.MapSyncService
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.paper.command.minimapCompanionCommand
import dev.slne.surf.minimapcompanion.paper.listener.CompanionListener
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.tracker.PaperTracker
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {

    override fun onEnable() {
        CompanionConfigs.init()

        for (channel in XaeroChannels.ALL) {
            server.messenger.registerOutgoingPluginChannel(this, channel)
            // The mods answer the handshake; registering the channels announces them, as a server with the mods does.
            server.messenger.registerIncomingPluginChannel(this, channel) { _, _, _ -> }
        }

        // JourneyMap's client only sends its requests on channels the server announced.
        for (channel in JourneyMapChannels.ANNOUNCED) {
            server.messenger.registerOutgoingPluginChannel(this, channel)
            server.messenger.registerIncomingPluginChannel(this, channel) { _, player, _ ->
                player.scheduler.run(this, { MapSyncService.answer(PaperCompanionPlayer(player), channel) }, null)
            }
        }

        CompanionListener.register()
        PaperTracker.configure()
        minimapCompanionCommand()

        for (player in server.onlinePlayers) {
            player.scheduler.run(this, {
                MapSyncService.sync(PaperCompanionPlayer(player))
                PaperTracker.reset(player)
            }, null)
            PaperTracker.track(player)
        }
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)
    }
}
