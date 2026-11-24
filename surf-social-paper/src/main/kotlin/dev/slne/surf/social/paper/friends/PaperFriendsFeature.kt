package dev.slne.surf.social.paper.friends

import dev.slne.surf.social.core.client.friends.FriendsClientInstance
import dev.slne.surf.social.paper.PaperFeature
import dev.slne.surf.social.paper.friends.command.friendCommand
import dev.slne.surf.social.paper.friends.command.subcommand.friend.FriendListCommand
import dev.slne.surf.social.paper.friends.command.subcommand.request.FriendRequestSendCommand
import dev.slne.surf.social.paper.friends.listener.ConnectionListener
import dev.slne.surf.social.paper.plugin

object PaperFriendsFeature : PaperFeature {
    override val name = "friends"

    override suspend fun onLoad() {
        val instance = FriendsClientInstance.INSTANCE

        instance.withEvents()
        instance.onLoad()
    }

    override suspend fun onEnable() {
        FriendsClientInstance.INSTANCE.onEnable()

        plugin.server.pluginManager.registerEvents(ConnectionListener(), plugin)

        friendCommand()
        FriendRequestSendCommand("fa").register()
        FriendListCommand("fl").register()
    }

    override suspend fun onDisable() {
        FriendsClientInstance.INSTANCE.onDisable()
    }
}
