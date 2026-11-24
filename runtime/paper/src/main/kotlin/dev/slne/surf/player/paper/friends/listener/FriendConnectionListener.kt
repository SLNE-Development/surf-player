package dev.slne.surf.player.paper.friends.listener

import dev.slne.surf.player.core.client.friends.connection.FriendConnectionNotifier
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Friends are notified network-wide from the proxy's join and quit (see
 * [FriendConnectionNotifier]); this server only reports joins for the greeting.
 */
object FriendConnectionListener : Listener {
    @EventHandler
    fun onConnect(event: PlayerJoinEvent) {
        FriendConnectionNotifier.onJoinedThisServer(event.player.uniqueId)
    }
}
