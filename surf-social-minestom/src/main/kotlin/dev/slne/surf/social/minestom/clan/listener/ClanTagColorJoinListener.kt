package dev.slne.surf.social.minestom.clan.listener

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.social.core.client.clan.tagcolor.ClanTagColorEnforcer
import kotlinx.coroutines.launch
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerSpawnEvent

/**
 * Checks the clan tag color of a joining player's clan against its owner's permissions.
 */
object ClanTagColorJoinListener : EventRegistrar {
    override fun register(node: EventNode<Event>) {
        node.addListener<PlayerSpawnEvent> { event ->
            if (!event.isFirstSpawn) return@addListener

            val playerUuid = event.player.uuid

            minestomAsyncScope.launch {
                ClanTagColorEnforcer.enforceForClanOf(playerUuid)
            }
        }
    }
}
