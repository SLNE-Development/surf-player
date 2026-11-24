package dev.slne.surf.player.minestom.friends.command.subcommand.friend

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.player.core.client.friends.command.friendListComponent
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions

fun friendListCommand(commandName: String) = CommandAPICommand(commandName).apply {
    withPermission(FriendPermissions.COMMAND_FRIEND_LIST)

    playerExecutorSuspend { player, _ ->
        player.sendMessage(friendListComponent(player.uuid))
    }
}
