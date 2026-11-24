package dev.slne.surf.social.paper.friends.command.subcommand.request

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.core.client.friends.command.friendRequestListComponent
import dev.slne.surf.social.core.client.friends.permission.FriendPermissions
import dev.slne.surf.social.paper.plugin

fun CommandAPICommand.friendRequestListCommand() = subcommand("requests") {
    withPermission(FriendPermissions.COMMAND_FRIEND_REQUEST_LIST)

    playerExecutor { player, _ ->
        plugin.launch {
            player.sendMessage(friendRequestListComponent(player.uniqueId))
        }
    }
}
