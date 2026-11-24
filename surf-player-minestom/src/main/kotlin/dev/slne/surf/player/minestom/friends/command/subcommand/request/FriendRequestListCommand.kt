package dev.slne.surf.player.minestom.friends.command.subcommand.request

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.core.client.friends.command.friendRequestListComponent
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions

fun CommandAPICommand.friendRequestListCommand(): CommandAPICommand = withSubcommand(
    subcommand("requests") {
        withPermission(FriendPermissions.COMMAND_FRIEND_REQUEST_LIST)

        playerExecutorSuspend { player, _ ->
            player.sendMessage(friendRequestListComponent(player.uuid))
        }
    }
)
