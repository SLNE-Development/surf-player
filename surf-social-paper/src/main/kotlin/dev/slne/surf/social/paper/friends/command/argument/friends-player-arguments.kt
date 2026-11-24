package dev.slne.surf.social.paper.friends.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.slne.surf.social.core.client.friends.command.NOT_FRIENDS_MESSAGE
import dev.slne.surf.social.core.client.friends.command.NO_RECEIVED_FRIEND_REQUEST_MESSAGE
import dev.slne.surf.social.core.client.friends.command.NO_SENT_FRIEND_REQUEST_MESSAGE
import dev.slne.surf.social.core.client.friends.command.findFriend
import dev.slne.surf.social.core.client.friends.command.findReceivedFriendRequestSender
import dev.slne.surf.social.core.client.friends.command.findSentFriendRequestTarget
import dev.slne.surf.social.core.client.friends.command.friendNames
import dev.slne.surf.social.core.client.friends.command.receivedFriendRequestNames
import dev.slne.surf.social.core.client.friends.command.sentFriendRequestNames

/** A friend of the sender. */
fun CommandAPICommand.friendArgument(nodeName: String): CommandAPICommand = withArguments(
    FriendsPlayerArgument(nodeName, ::findFriend, ::friendNames, NOT_FRIENDS_MESSAGE)
)

/** A player who sent the sender a friend request. */
fun CommandAPICommand.receivedFriendRequestArgument(nodeName: String): CommandAPICommand = withArguments(
    FriendsPlayerArgument(
        nodeName,
        ::findReceivedFriendRequestSender,
        ::receivedFriendRequestNames,
        NO_RECEIVED_FRIEND_REQUEST_MESSAGE
    )
)

/** A player the sender sent a friend request to. */
fun CommandAPICommand.sentFriendRequestArgument(nodeName: String): CommandAPICommand = withArguments(
    FriendsPlayerArgument(
        nodeName,
        ::findSentFriendRequestTarget,
        ::sentFriendRequestNames,
        NO_SENT_FRIEND_REQUEST_MESSAGE
    )
)
