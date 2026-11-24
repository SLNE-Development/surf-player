package dev.slne.surf.social.paper.friends

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.social.core.client.friends.FriendsClientInstance
import dev.slne.surf.social.paper.friends.command.friendCommand
import dev.slne.surf.social.paper.friends.command.subcommand.friend.FriendListCommand
import dev.slne.surf.social.paper.friends.command.subcommand.request.FriendRequestSendCommand
import dev.slne.surf.social.paper.friends.listener.ConnectionListener
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(SurfFriendsPaper::class.java)

class SurfFriendsPaper : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        val instance = FriendsClientInstance.INSTANCE

        instance.withEvents()
        instance.onLoad()
    }

    override suspend fun onEnableAsync() {
        FriendsClientInstance.INSTANCE.onEnable()

        server.pluginManager.registerEvents(ConnectionListener(), this)

        friendCommand()
        FriendRequestSendCommand("fa").register()
        FriendListCommand("fl").register()
    }

    override suspend fun onDisableAsync() {
        FriendsClientInstance.INSTANCE.onDisable()
    }
}

