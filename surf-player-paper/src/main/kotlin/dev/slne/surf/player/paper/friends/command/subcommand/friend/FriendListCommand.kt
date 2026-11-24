package dev.slne.surf.player.paper.friends.command.subcommand.friend

import dev.jorel.commandapi.CommandAPICommand
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.friends.command.friendListComponent
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions

class FriendListCommand(commandName: String) : CommandAPICommand(commandName) {
    init {
        withPermission(FriendPermissions.COMMAND_FRIEND_LIST)

        playerExecutorSuspend { player, _ ->
            player.sendMessage(friendListComponent(player.uniqueId))
        }
    }
}
