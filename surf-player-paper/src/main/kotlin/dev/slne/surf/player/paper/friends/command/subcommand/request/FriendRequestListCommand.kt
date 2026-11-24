package dev.slne.surf.player.paper.friends.command.subcommand.request

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.friends.command.friendRequestListComponent
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions

fun CommandAPICommand.friendRequestListCommand() = subcommand("requests") {
    withPermission(FriendPermissions.COMMAND_FRIEND_REQUEST_LIST)

    playerExecutorSuspend { player, _ ->
        player.sendMessage(friendRequestListComponent(player.uniqueId))
    }
}
