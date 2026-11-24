package dev.slne.surf.player.minestom.friends.command

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.minestom.command.visibleOfflinePlayerArgument
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.core.client.friends.command.*
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.minestom.command.asViewer
import dev.slne.surf.player.minestom.command.CONFIRMATION_LIFETIME
import dev.slne.surf.player.minestom.command.PlayerLookupArgument
import dev.slne.surf.player.minestom.command.helpExecutor
import dev.slne.surf.player.minestom.command.page
import dev.slne.surf.player.minestom.command.pageArgument
import dev.slne.surf.player.minestom.command.playerClickEvent
import kotlinx.coroutines.Deferred
import net.kyori.adventure.text.Component

private const val PLAYER = "spieler"
private const val FRIEND = "freund"

private suspend fun CommandArguments.resolve(node: String, notFound: Component): SurfCommunityPlayer =
    get<Deferred<SurfCommunityPlayer?>>(node).await() ?: CommandAPI.failWithMessage(notFound)

/**
 * `/friend` with every subcommand; there are no short forms.
 */
fun registerFriendCommands() {
    commandAPICommand("friend") {
        withPermission(FriendPermissions.COMMAND_FRIEND)
        helpExecutor(friendCommandHelp)

        withSubcommand(friendAddCommand("add"))
        withSubcommand(friendListCommand("list"))

        withSubcommand(subcommand("info") {
            withPermission(FriendPermissions.COMMAND_INFO)
            withArguments(PlayerLookupArgument(FRIEND, ::findFriend, ::friendNames))
            playerExecutorSuspend { player, args ->
                val friend = args.resolve(FRIEND, NOT_FRIENDS_MESSAGE)
                player.sendMessage(friendInfoComponent(player.uuid, friend.uuid))
            }
        })

        withSubcommand(subcommand("remove") {
            withPermission(FriendPermissions.COMMAND_REMOVE)
            withArguments(PlayerLookupArgument(FRIEND, ::findFriend, ::friendNames))
            playerExecutorSuspend { player, args ->
                val friend = args.resolve(FRIEND, NOT_FRIENDS_MESSAGE).uuid
                val click = playerClickEvent(CONFIRMATION_LIFETIME) { clicked ->
                    handleFriendRemoveClick(clicked, clicked.uuid, friend)
                }

                player.sendMessage(friendRemoveConfirmation(friend, click))
            }
        })

        withSubcommand(subcommand("requests") {
            withPermission(FriendPermissions.COMMAND_REQUESTS)
            pageArgument()
            playerExecutorSuspend { player, args ->
                player.sendMessage(friendRequestListComponent(player.uuid, args.page))
            }
        })

        withSubcommand(subcommand("accept") {
            withPermission(FriendPermissions.COMMAND_ACCEPT)
            withArguments(PlayerLookupArgument(PLAYER, ::findReceivedFriendRequestSender, ::receivedFriendRequestNames))
            playerExecutorSuspend { player, args ->
                val sender = args.resolve(PLAYER, NO_RECEIVED_FRIEND_REQUEST_MESSAGE)
                SurfCommunityPlayer(player.uuid).acceptFriendRequest(sender.uuid)
            }
        })

        withSubcommand(subcommand("deny") {
            withPermission(FriendPermissions.COMMAND_DENY)
            withArguments(PlayerLookupArgument(PLAYER, ::findReceivedFriendRequestSender, ::receivedFriendRequestNames))
            playerExecutorSuspend { player, args ->
                val sender = args.resolve(PLAYER, NO_RECEIVED_FRIEND_REQUEST_MESSAGE)
                SurfCommunityPlayer(player.uuid).declineFriendRequest(sender.uuid)
            }
        })

        withSubcommand(subcommand("revoke") {
            withPermission(FriendPermissions.COMMAND_REVOKE)
            withArguments(PlayerLookupArgument(PLAYER, ::findSentFriendRequestTarget, ::sentFriendRequestNames))
            playerExecutorSuspend { player, args ->
                val target = args.resolve(PLAYER, NO_SENT_FRIEND_REQUEST_MESSAGE)
                SurfCommunityPlayer(player.uuid).revokeFriendRequest(target.uuid)
            }
        })
    }
}

/** `/friend add <spieler>`. */
private fun friendAddCommand(name: String): CommandAPICommand = subcommand(name) {
    withPermission(FriendPermissions.COMMAND_ADD)
    visibleOfflinePlayerArgument(PLAYER)
    playerExecutorSuspend { player, args ->
        val target = args.get<Deferred<SurfPlayer?>>(PLAYER).await()
        sendFriendRequest(player.uuid, target)?.let(player::sendMessage)
    }
}

/** `/friend list [seite]`. */
private fun friendListCommand(name: String): CommandAPICommand = subcommand(name) {
    withPermission(FriendPermissions.COMMAND_LIST)
    pageArgument()
    playerExecutorSuspend { player, args ->
        player.sendMessage(friendListComponent(player.uuid, args.page, player.asViewer()))
    }
}
