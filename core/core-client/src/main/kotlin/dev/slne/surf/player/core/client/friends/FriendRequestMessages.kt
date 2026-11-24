package dev.slne.surf.player.core.client.friends

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.result.FriendRequestStateResult
import dev.slne.surf.player.core.client.friends.command.NO_RECEIVED_FRIEND_REQUEST_MESSAGE
import dev.slne.surf.player.api.friends.utils.displayName
import net.kyori.adventure.text.Component

/**
 * What the player who answered a friend request (by command or by clicking [✔]/[❌]) sees when it
 * failed; `null` on success, whose confirmation every server sends for the
 * `REQUEST_ACCEPTED`/`REQUEST_DECLINED` event.
 */
fun friendRequestAnswerFailureMessage(result: FriendRequestStateResult): Component? = when (result) {
    FriendRequestStateResult.Success -> null
    is FriendRequestStateResult.NoFriendRequest -> NO_RECEIVED_FRIEND_REQUEST_MESSAGE
}

/**
 * The message the sender sees for [FriendRequestCreateResult.RequestsDisabled]: [target] turned
 * off the setting `friend:requests`.
 */
fun friendRequestsDisabledMessage(target: SurfPlayer?): Component = buildText {
    appendErrorPrefix()
    append(target.displayName())
    error(" nimmt keine Freundschaftsanfragen an.")
}
