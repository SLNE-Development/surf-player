package dev.slne.surf.player.minestom.friends.command.subcommand.request

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.minestom.friends.command.receivedFriendRequestArgument
import dev.slne.surf.player.minestom.friends.command.resolveReceivedFriendRequestSender

fun CommandAPICommand.friendRequestDeclineCommand(): CommandAPICommand = withSubcommand(
    subcommand("decline") {
        withPermission(FriendPermissions.COMMAND_FRIEND_REQUEST_DECLINE)

        receivedFriendRequestArgument("target")

        playerExecutorSuspend { player, args ->
            val target = args.resolveReceivedFriendRequestSender("target")

            SurfCommunityPlayer(player.uuid).declineFriendRequest(target.uuid)
        }
    }
)
