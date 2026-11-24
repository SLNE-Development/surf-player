package dev.slne.surf.social.minestom.friends.command.subcommand.friend

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.api.friends.player.FriendsPlayer
import dev.slne.surf.social.core.client.friends.permission.FriendPermissions
import dev.slne.surf.social.minestom.friends.command.argument.friend.friendArgument
import dev.slne.surf.social.minestom.friends.command.resolveFriend

fun CommandAPICommand.friendRemoveCommand(): CommandAPICommand = withSubcommand(
    subcommand("remove") {
        withPermission(FriendPermissions.COMMAND_FRIEND_REMOVE)

        friendArgument("target")

        playerExecutorSuspend { player, args ->
            val target = args.resolveFriend("target")

            FriendsPlayer[player.uuid].removeFriendship(target)
        }
    }
)
