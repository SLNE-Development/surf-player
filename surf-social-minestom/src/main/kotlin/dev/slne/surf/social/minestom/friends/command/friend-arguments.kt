package dev.slne.surf.social.minestom.friends.command

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.social.core.client.friends.command.findFriend
import dev.slne.surf.social.core.client.friends.command.findReceivedFriendRequestSender
import dev.slne.surf.social.core.client.friends.command.findSentFriendRequestTarget
import dev.slne.surf.social.core.client.friends.command.friendNames
import dev.slne.surf.social.core.client.friends.command.receivedFriendRequestNames
import dev.slne.surf.social.core.client.friends.command.sentFriendRequestNames
import dev.slne.surf.social.minestom.command.PlayerLookupArgument

/**
 * Adds an argument naming one of the executor's friends, see [resolveFriend].
 */
fun CommandAPICommand.friendArgument(nodeName: String): CommandAPICommand =
    withArguments(PlayerLookupArgument(nodeName, ::findFriend, ::friendNames))

/**
 * Adds an argument naming a player who has sent the executor a friend request, see
 * [resolveReceivedFriendRequestSender].
 */
fun CommandAPICommand.receivedFriendRequestArgument(nodeName: String): CommandAPICommand =
    withArguments(
        PlayerLookupArgument(
            nodeName,
            ::findReceivedFriendRequestSender,
            ::receivedFriendRequestNames
        )
    )

/**
 * Adds an argument naming a player the executor has sent a friend request to, see
 * [resolveSentFriendRequestTarget].
 */
fun CommandAPICommand.sentFriendRequestArgument(nodeName: String): CommandAPICommand =
    withArguments(
        PlayerLookupArgument(nodeName, ::findSentFriendRequestTarget, ::sentFriendRequestNames)
    )
