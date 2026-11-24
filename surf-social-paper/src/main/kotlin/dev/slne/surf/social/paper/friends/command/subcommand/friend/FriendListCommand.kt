package dev.slne.surf.social.paper.friends.command.subcommand.friend

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.social.core.client.friends.command.friendListComponent
import dev.slne.surf.social.core.client.friends.permission.FriendPermissions
import dev.slne.surf.social.paper.friends.plugin

class FriendListCommand(commandName: String) : CommandAPICommand(commandName) {
    init {
        withPermission(FriendPermissions.COMMAND_FRIEND_LIST)

        playerExecutor { player, _ ->
            plugin.launch {
                player.sendMessage(friendListComponent(player.uniqueId))
            }
        }
    }
}
