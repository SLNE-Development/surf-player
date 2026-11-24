package dev.slne.surf.player.velocity.friends

import dev.slne.surf.player.velocity.PlayerVelocityPlugin
import dev.slne.surf.player.velocity.VelocityPlayerFeature
import dev.slne.surf.player.velocity.friends.listener.FriendNetworkConnectionListener
import dev.slne.surf.player.velocity.plugin

/**
 * The friends feature of [PlayerVelocityPlugin]: reports network joins and quits, which only the
 * proxy can tell apart from server switches, to every server.
 */
object FriendsVelocityFeature : VelocityPlayerFeature {
    override val id = "friends"

    override suspend fun onEnable() {
        plugin.eventManager.register(plugin, FriendNetworkConnectionListener)
    }
}
