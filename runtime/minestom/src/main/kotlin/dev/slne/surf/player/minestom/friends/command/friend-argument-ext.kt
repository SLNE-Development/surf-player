package dev.slne.surf.player.minestom.friends.command

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.core.client.friends.command.NOT_FRIENDS_MESSAGE
import dev.slne.surf.player.core.client.friends.command.NO_RECEIVED_FRIEND_REQUEST_MESSAGE
import dev.slne.surf.player.core.client.friends.command.NO_SENT_FRIEND_REQUEST_MESSAGE
import kotlinx.coroutines.Deferred

/**
 * The friend the [node] argument names.
 */
suspend fun CommandArguments.resolveFriend(node: String): SurfCommunityPlayer =
    get<Deferred<SurfCommunityPlayer?>>(node).await()
        ?: CommandAPI.failWithMessage(NOT_FRIENDS_MESSAGE)

/**
 * The player the [node] argument names, who has sent the executor a friend request.
 */
suspend fun CommandArguments.resolveReceivedFriendRequestSender(node: String): SurfCommunityPlayer =
    get<Deferred<SurfCommunityPlayer?>>(node).await()
        ?: CommandAPI.failWithMessage(NO_RECEIVED_FRIEND_REQUEST_MESSAGE)

/**
 * The player the [node] argument names, whom the executor has sent a friend request to.
 */
suspend fun CommandArguments.resolveSentFriendRequestTarget(node: String): SurfCommunityPlayer =
    get<Deferred<SurfCommunityPlayer?>>(node).await()
        ?: CommandAPI.failWithMessage(NO_SENT_FRIEND_REQUEST_MESSAGE)
