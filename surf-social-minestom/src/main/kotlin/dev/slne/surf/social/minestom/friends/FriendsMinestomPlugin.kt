package dev.slne.surf.social.minestom.friends

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.core.client.friends.FriendsClientInstance
import dev.slne.surf.social.minestom.friends.command.friendCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.friend.friendListCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.request.friendRequestSendCommand
import dev.slne.surf.social.minestom.friends.listener.ConnectionListener

object FriendsMinestomPlugin : SurfMinestomPlugin() {
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

        ConnectionListener.register(eventNode)
    }

    override suspend fun onDisable() {
        FriendsClientInstance.INSTANCE.onDisable()
    }
}
