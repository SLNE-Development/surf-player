package dev.slne.surf.social.minestom.friends

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.social.core.client.friends.FriendsClientInstance
import dev.slne.surf.social.core.client.friends.connection.FriendConnectionNotifier
import dev.slne.surf.social.minestom.MinestomSocialFeature
import dev.slne.surf.social.minestom.friends.command.friendCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.friend.friendListCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.request.friendRequestSendCommand
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

object FriendsMinestomFeature : MinestomSocialFeature {
    override val id = "friends"

    override suspend fun onLoad() {
        val instance = FriendsClientInstance.INSTANCE

        instance.withEvents()
        instance.onLoad()
    }

    override suspend fun onEnable() {
        FriendsClientInstance.INSTANCE.onEnable()
        friendCommand()

        friendRequestSendCommand("fa").register()
        friendListCommand("fl").register()
    }

    override suspend fun onDisable() {
        FriendsClientInstance.INSTANCE.onDisable()
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
