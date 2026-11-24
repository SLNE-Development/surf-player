package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.core.client.friends.util.resolveUsername
import java.util.*

/**
 * The message shown when the named player is not a friend of the executor.
 */
val NOT_FRIENDS_MESSAGE = buildText {
    appendErrorPrefix()
    error("Du bist nicht mit diesem Spieler befreundet.")
}

/**
 * The message shown when the named player has not sent a friend request to the executor.
 */
val NO_RECEIVED_FRIEND_REQUEST_MESSAGE = buildText {
    appendErrorPrefix()
    error("Du hast keine Freundschaftsanfrage von diesem Spieler erhalten.")
}

/**
 * The message shown when the executor has not sent a friend request to the named player.
 */
val NO_SENT_FRIEND_REQUEST_MESSAGE = buildText {
    appendErrorPrefix()
    error("Du hast keine Freundschaftsanfrage an diesen Spieler gesendet.")
}

/**
 * The friend of [player] named [input], matched regardless of case.
 */
suspend fun findFriend(player: UUID, input: String): SurfCommunityPlayer? {
    val friendship = SurfCommunityPlayer(player).friendships.firstOrNull { friendship ->
        resolveUsername(friendship.friendUuid).equals(input, ignoreCase = true)
    } ?: return null

    return SurfCommunityPlayer(friendship.friendUuid)
}

/**
 * The names of the friends of [player].
 */
suspend fun friendNames(player: UUID): List<String> =
    SurfCommunityPlayer(player).friendships.mapNotNull { friendship ->
        resolveUsername(friendship.friendUuid)
    }

/**
 * The player named [input] who has sent [player] a friend request, matched regardless of case.
 */
suspend fun findReceivedFriendRequestSender(player: UUID, input: String): SurfCommunityPlayer? {
    val request = SurfCommunityPlayer(player).receivedFriendRequests.firstOrNull { request ->
        resolveUsername(request.senderUuid).equals(input, ignoreCase = true)
    } ?: return null

    return SurfCommunityPlayer(request.senderUuid)
}

/**
 * The names of the players who have sent [player] a friend request.
 */
suspend fun receivedFriendRequestNames(player: UUID): List<String> =
    SurfCommunityPlayer(player).receivedFriendRequests.mapNotNull { request ->
        resolveUsername(request.senderUuid)
    }

/**
 * The player named [input] whom [player] has sent a friend request to, matched regardless of case.
 */
suspend fun findSentFriendRequestTarget(player: UUID, input: String): SurfCommunityPlayer? {
    val request = SurfCommunityPlayer(player).sentFriendRequests.firstOrNull { request ->
        resolveUsername(request.targetUuid).equals(input, ignoreCase = true)
    } ?: return null

    return SurfCommunityPlayer(request.targetUuid)
}

/**
 * The names of the players whom [player] has sent a friend request to.
 */
suspend fun sentFriendRequestNames(player: UUID): List<String> =
    SurfCommunityPlayer(player).sentFriendRequests.mapNotNull { request ->
        resolveUsername(request.targetUuid)
    }
