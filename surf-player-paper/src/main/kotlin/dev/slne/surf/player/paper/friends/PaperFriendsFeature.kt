package dev.slne.surf.social.paper.friends

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.social.core.client.friends.FriendsClientInstance
import dev.slne.surf.social.paper.PaperSocialFeature
import dev.slne.surf.social.paper.friends.command.friendCommand
import dev.slne.surf.social.paper.friends.command.subcommand.friend.FriendListCommand
import dev.slne.surf.social.paper.friends.command.subcommand.request.FriendRequestSendCommand
import dev.slne.surf.social.paper.friends.listener.FriendConnectionListener

object PaperFriendsFeature : PaperSocialFeature {
    override val name = "friends"

    override suspend fun onLoad() {
        val instance = FriendsClientInstance.INSTANCE

        instance.withEvents()
        instance.onLoad()
    }

    override suspend fun onEnable() {
        FriendsClientInstance.INSTANCE.onEnable()

        FriendConnectionListener.register()

        friendCommand()
        FriendRequestSendCommand("fa").register()
        FriendListCommand("fl").register()
    }

    override suspend fun onDisable() {
        FriendsClientInstance.INSTANCE.onDisable()
    }
}
