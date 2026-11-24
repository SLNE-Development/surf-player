package dev.slne.surf.player.paper.friends

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.friends.FriendsClient
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.friends.command.registerFriendCommands
import dev.slne.surf.player.paper.friends.listener.FriendConnectionListener

object PaperFriendsFeature : PaperPlayerFeature {
    override val name = "friends"

    override suspend fun onLoad() {
        FriendsClient.load()
    }

    override suspend fun onEnable() {
        FriendsClient.enable()

        FriendConnectionListener.register()

        registerFriendCommands()
    }
}
