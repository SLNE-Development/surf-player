package dev.slne.surf.social.paper.friends.command.subcommand.friend

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.api.friends.player.FriendsPlayer
import dev.slne.surf.social.core.client.friends.command.friendInfoComponent
import dev.slne.surf.social.core.client.friends.permission.FriendPermissions
import dev.slne.surf.social.paper.friends.command.argument.friend.friendArgument

fun CommandAPICommand.friendInfoCommand() = subcommand("info") {
    withPermission(FriendPermissions.COMMAND_FRIEND_INFO)

    friendArgument("target")

    playerExecutorSuspend { player, args ->
        val target = args.awaiting<FriendsPlayer>("target")

        player.sendMessage(friendInfoComponent(player.uniqueId, target))
    }
}
