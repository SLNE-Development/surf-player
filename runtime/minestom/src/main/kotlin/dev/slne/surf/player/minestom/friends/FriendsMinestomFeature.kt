package dev.slne.surf.player.minestom.friends

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.player.core.client.friends.FriendsClient
import dev.slne.surf.player.core.client.friends.connection.FriendConnectionNotifier
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.friends.command.friendCommand
import dev.slne.surf.player.minestom.friends.command.subcommand.friend.friendListCommand
import dev.slne.surf.player.minestom.friends.command.subcommand.request.friendRequestSendCommand
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

object FriendsMinestomFeature : MinestomPlayerFeature {
    override val id = "friends"

    override suspend fun onLoad() {
        FriendsClient.load()
    }

    override suspend fun onEnable() {
        FriendsClient.enable()
        friendCommand()

        friendRequestSendCommand("fa").register()
        friendListCommand("fl").register()
    }

    override fun onPlayerJoin(player: Player) {
        val uuid = player.uuid
        val onlineFriends = FriendConnectionNotifier.greet(uuid)

        minestomAsyncScope.launch {
            FriendConnectionNotifier.announceOnline(uuid, onlineFriends)
        }
    }

    override fun onPlayerQuit(player: Player) {
        val uuid = player.uuid

        minestomAsyncScope.launch {
            FriendConnectionNotifier.announceOffline(uuid)
        }
    }
}
