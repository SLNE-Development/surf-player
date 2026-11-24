package dev.slne.surf.social.minestom.minimap.listener

import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.social.minestom.minimap.platform.RegisteredChannels
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerSkinInitEvent

/**
 * Announces the map mods' channels, as a server with the mods does. The mods answer the handshake and ask for
 * the world ID only on channels the server announced.
 */
object ChannelAnnounceListener : EventRegistrar {
    const val REGISTER_CHANNEL = "minecraft:register"

    override fun register(node: EventNode<Event>) {
        // The only event between the join game packet and the spawn, which waits for the spawn chunks to load.
        // VoxelMap asks for the world ID once, on its first tick in the new world, so the channels must reach it
        // together with the join, as on Paper.
        node.addListener<PlayerSkinInitEvent> { event ->
            event.player.sendPluginMessage(REGISTER_CHANNEL, RegisteredChannels.registerPayload(RegisteredChannels.MOD_CHANNELS))
        }
    }
}
