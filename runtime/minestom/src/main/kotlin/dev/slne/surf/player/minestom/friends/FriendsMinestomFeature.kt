package dev.slne.surf.player.minestom.friends

import dev.slne.surf.player.core.client.friends.FriendsClient
import dev.slne.surf.player.core.client.friends.connection.FriendConnectionNotifier
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.friends.command.registerFriendCommands
import net.minestom.server.entity.Player

object FriendsMinestomFeature : MinestomPlayerFeature {
    override val id = "friends"

    override suspend fun onLoad() {
        FriendsClient.load()
    }

    override suspend fun onEnable() {
        FriendsClient.enable()
        registerFriendCommands()
    }

    /** Friends are notified network-wide from the proxy's join and quit, see FriendConnectionNotifier. */
    override fun onPlayerJoin(player: Player) {
        FriendConnectionNotifier.onJoinedThisServer(player.uuid)
    }
}
