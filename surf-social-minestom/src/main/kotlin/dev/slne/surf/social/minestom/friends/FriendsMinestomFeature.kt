package dev.slne.surf.social.minestom.friends

import dev.slne.surf.social.minestom.MinestomSocialFeature
import dev.slne.surf.social.minestom.SocialMinestomPlugin
import dev.slne.surf.social.core.client.friends.FriendsClientInstance
import dev.slne.surf.social.minestom.friends.command.friendCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.friend.friendListCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.request.friendRequestSendCommand
import dev.slne.surf.social.minestom.friends.listener.ConnectionListener

object FriendsMinestomFeature : MinestomSocialFeature {
    override val id = "friends"

    val dataDirectory get() = SocialMinestomPlugin.featureDataDirectory(this)

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

        ConnectionListener.register(SocialMinestomPlugin.eventNode)
    }

    override suspend fun onDisable() {
        FriendsClientInstance.INSTANCE.onDisable()
    }
}
