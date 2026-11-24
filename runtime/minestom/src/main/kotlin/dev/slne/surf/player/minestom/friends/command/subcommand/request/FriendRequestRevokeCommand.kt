package dev.slne.surf.player.minestom.friends.command.subcommand.request

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.minestom.friends.command.sentFriendRequestArgument
import dev.slne.surf.player.minestom.friends.command.resolveSentFriendRequestTarget

fun CommandAPICommand.friendRequestRevokeCommand(): CommandAPICommand = withSubcommand(
    subcommand("revoke") {
        withPermission(FriendPermissions.COMMAND_FRIEND_REQUEST_REVOKE)

        sentFriendRequestArgument("target")

        playerExecutorSuspend { player, args ->
            val target = args.resolveSentFriendRequestTarget("target")

            SurfCommunityPlayer(player.uuid).revokeFriendRequest(target.uuid)
        }
    }
)
