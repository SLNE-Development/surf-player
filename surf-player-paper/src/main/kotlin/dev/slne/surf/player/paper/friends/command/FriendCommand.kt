package dev.slne.surf.social.paper.friends.command

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.paper.friends.command.subcommand.friend.FriendListCommand
import dev.slne.surf.social.paper.friends.command.subcommand.friend.friendInfoCommand
import dev.slne.surf.social.paper.friends.command.subcommand.friend.friendRemoveCommand
import dev.slne.surf.social.paper.friends.command.subcommand.request.*
import dev.slne.surf.social.core.client.friends.permission.FriendPermissions

fun friendCommand() = commandAPICommand("friend") {
    withPermission(FriendPermissions.COMMAND_FRIEND)
    withAliases("f")

    friendRemoveCommand()
    friendInfoCommand()

    friendRequestAcceptCommand()
    friendRequestDeclineCommand()
    friendRequestRevokeCommand()
    friendRequestListCommand()

    subcommand(FriendListCommand("list"))
    subcommand(FriendRequestSendCommand("add"))
}

