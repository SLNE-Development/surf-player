package dev.slne.surf.player.minestom.playtime.listener

import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.player.core.client.playtime.afk.AfkTracker
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerMoveEvent

/**
 * Records players looking around as activity for the afk detection.
 */
object PlayerAfkListener : EventRegistrar {
    override fun register(node: EventNode<Event>) {
        node.addListener<PlayerMoveEvent> { event ->
            val position = event.player.position
            val newPosition = event.newPosition

            if (newPosition.yaw == position.yaw && newPosition.pitch == position.pitch) {
                return@addListener
            }

            AfkTracker.recordActivity(event.player.uuid)
        }
    }
}
