package dev.slne.surf.player.paper.friends.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.core.client.friends.command.*
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.paper.command.asViewer
import dev.slne.surf.player.paper.command.CONFIRMATION_LIFETIME
import dev.slne.surf.player.paper.command.helpExecutor
import dev.slne.surf.player.paper.command.page
import dev.slne.surf.player.paper.command.pageArgument
import dev.slne.surf.player.paper.command.playerClickEvent
import dev.slne.surf.player.paper.command.visibleOfflinePlayerArgument
import dev.slne.surf.player.paper.friends.command.argument.FriendsPlayerArgument

private const val PLAYER = "spieler"
private const val FRIEND = "freund"

/**
 * `/friend` with every subcommand; there are no short forms.
 */
fun registerFriendCommands() {
    commandAPICommand("friend") {
        withPermission(FriendPermissions.COMMAND_FRIEND)
        helpExecutor(friendCommandHelp)

        subcommand(friendAddCommand("add"))
        subcommand(friendListCommand("list"))

        subcommand("info") {
            withPermission(FriendPermissions.COMMAND_INFO)
            withArguments(friendArgument())
            playerExecutorSuspend { player, args ->
                val friend = args.awaiting<SurfCommunityPlayer>(FRIEND)
                player.sendMessage(friendInfoComponent(player.uniqueId, friend.uuid))
            }
        }

        subcommand("remove") {
            withPermission(FriendPermissions.COMMAND_REMOVE)
            withArguments(friendArgument())
            playerExecutorSuspend { player, args ->
                val friend = args.awaiting<SurfCommunityPlayer>(FRIEND).uuid
                val click = playerClickEvent(CONFIRMATION_LIFETIME) { clicked ->
                    handleFriendRemoveClick(clicked, clicked.uniqueId, friend)
                }

                player.sendMessage(friendRemoveConfirmation(friend, click))
            }
        }

        subcommand("requests") {
            withPermission(FriendPermissions.COMMAND_REQUESTS)
            pageArgument()
            playerExecutorSuspend { player, args ->
                player.sendMessage(friendRequestListComponent(player.uniqueId, args.page))
            }
        }

        subcommand("accept") {
            withPermission(FriendPermissions.COMMAND_ACCEPT)
            withArguments(receivedRequestArgument())
            playerExecutorSuspend { player, args ->
                val sender = args.awaiting<SurfCommunityPlayer>(PLAYER)
                SurfCommunityPlayer(player.uniqueId).acceptFriendRequest(sender.uuid)
            }
        }

        subcommand("deny") {
            withPermission(FriendPermissions.COMMAND_DENY)
            withArguments(receivedRequestArgument())
            playerExecutorSuspend { player, args ->
                val sender = args.awaiting<SurfCommunityPlayer>(PLAYER)
                SurfCommunityPlayer(player.uniqueId).declineFriendRequest(sender.uuid)
            }
        }

        subcommand("revoke") {
            withPermission(FriendPermissions.COMMAND_REVOKE)
            withArguments(
                FriendsPlayerArgument(
                    PLAYER,
                    ::findSentFriendRequestTarget,
                    ::sentFriendRequestNames,
                    NO_SENT_FRIEND_REQUEST_MESSAGE
                )
            )
            playerExecutorSuspend { player, args ->
                val target = args.awaiting<SurfCommunityPlayer>(PLAYER)
                SurfCommunityPlayer(player.uniqueId).revokeFriendRequest(target.uuid)
            }
        }
    }
}

/** `/friend add <spieler>`. */
private fun friendAddCommand(name: String) = CommandAPICommand(name).apply {
    withPermission(FriendPermissions.COMMAND_ADD)
    visibleOfflinePlayerArgument(PLAYER)
    playerExecutorSuspend { player, args ->
        val target = args.awaitingOrNull<SurfPlayer?>(PLAYER)
        sendFriendRequest(player.uniqueId, target)?.let(player::sendMessage)
    }
}

/** `/friend list [seite]`. */
private fun friendListCommand(name: String) = CommandAPICommand(name).apply {
    withPermission(FriendPermissions.COMMAND_LIST)
    pageArgument()
    playerExecutorSuspend { player, args ->
        player.sendMessage(friendListComponent(player.uniqueId, args.page, player.asViewer()))
    }
}

private fun friendArgument() =
    FriendsPlayerArgument(FRIEND, ::findFriend, ::friendNames, NOT_FRIENDS_MESSAGE)

private fun receivedRequestArgument() = FriendsPlayerArgument(
    PLAYER,
    ::findReceivedFriendRequestSender,
    ::receivedFriendRequestNames,
    NO_RECEIVED_FRIEND_REQUEST_MESSAGE
)
