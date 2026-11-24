package dev.slne.surf.player.paper.minimap.listener

import dev.slne.surf.player.core.client.minimap.journeymap.JourneyMapChannels
import dev.slne.surf.player.core.client.minimap.service.MapSyncService
import dev.slne.surf.player.core.client.minimap.voxelmap.VoxelMapChannels
import dev.slne.surf.player.core.client.minimap.xaero.XaeroChannels
import dev.slne.surf.player.paper.minimap.platform.PaperMinimapPlayer
import dev.slne.surf.player.paper.plugin

/**
 * Registers the map mods' plugin channels, as a server with the mods does, and answers the requests clients send
 * on them.
 */
object PluginChannelListener {

    fun register() {
        val messenger = plugin.server.messenger

        for (channel in XaeroChannels.ALL) {
            messenger.registerOutgoingPluginChannel(plugin, channel)
            // The mods answer the handshake; registering the channels announces them, as a server with the mods does.
            messenger.registerIncomingPluginChannel(plugin, channel) { _, _, _ -> }
        }

        // JourneyMap's client only sends its requests on channels the server announced, and VoxelMap's only asks
        // for the world ID on the shared channel among them.
        for (channel in JourneyMapChannels.ANNOUNCED) {
            messenger.registerOutgoingPluginChannel(plugin, channel)
            messenger.registerIncomingPluginChannel(plugin, channel) { _, player, _ ->
                player.scheduler.run(plugin, { MapSyncService.answer(PaperMinimapPlayer(player), channel) }, null)
            }
        }

        // VoxelMap's server part only sends on its channel; announcing it costs nothing.
        messenger.registerOutgoingPluginChannel(plugin, VoxelMapChannels.SETTINGS)
        messenger.registerIncomingPluginChannel(plugin, VoxelMapChannels.SETTINGS) { _, _, _ -> }
    }

    /**
     * Unregisters only the map mods' channels; other features of the plugin may use channels of their own.
     */
    fun unregister() {
        val messenger = plugin.server.messenger

        for (channel in XaeroChannels.ALL + JourneyMapChannels.ANNOUNCED + VoxelMapChannels.SETTINGS) {
            messenger.unregisterOutgoingPluginChannel(plugin, channel)
            messenger.unregisterIncomingPluginChannel(plugin, channel)
        }
    }
}
