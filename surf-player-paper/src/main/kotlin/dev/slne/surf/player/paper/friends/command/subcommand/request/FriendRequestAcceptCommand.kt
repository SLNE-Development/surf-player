package dev.slne.surf.player.paper.friends.command.subcommand.request

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.api.friends.player.FriendsPlayer
import dev.slne.surf.player.paper.friends.command.argument.receivedFriendRequestArgument
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions

fun CommandAPICommand.friendRequestAcceptCommand() = subcommand("accept") {
    withPermission(FriendPermissions.COMMAND_FRIEND_REQUEST_ACCEPT)

    receivedFriendRequestArgument("target")

    playerExecutorSuspend { player, args ->
        val target = args.awaiting<FriendsPlayer>("target")
        val playerFriendsPlayer = FriendsPlayer[player.uniqueId]

        playerFriendsPlayer.acceptFriendRequest(target)
    }
}
