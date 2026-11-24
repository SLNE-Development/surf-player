package dev.slne.surf.social.minestom.friends.command

import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.social.core.client.friends.permission.FriendPermissions
import dev.slne.surf.social.minestom.friends.command.subcommand.friend.friendListCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.friend.friendInfoCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.friend.friendRemoveCommand
import dev.slne.surf.social.minestom.friends.command.subcommand.request.*

fun friendCommand() = commandAPICommand("friend") {
    withPermission(FriendPermissions.COMMAND_FRIEND)
    withAliases("f")

    friendRemoveCommand()
    friendInfoCommand()

    friendRequestAcceptCommand()
    friendRequestDeclineCommand()
    friendRequestRevokeCommand()
    friendRequestListCommand()

    withSubcommand(friendListCommand("list"))
    withSubcommand(friendRequestSendCommand("add"))
}
