package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.clickRunsCommand
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.utils.displayName
import dev.slne.surf.player.core.client.friends.util.formatRemainingComponent
import dev.slne.surf.player.core.client.util.undeliverableMessage
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.time.Duration

/**
 * Sends a friend request from [sender] to [target].
 *
 * Returns the message to show [sender] when no request was sent, and `null` once one was.
 */
suspend fun sendFriendRequest(sender: UUID, target: SurfPlayer?): Component? {
    if (target == null) {
        return buildText {
            appendErrorPrefix()
            error("Der angegebene Spieler wurde nicht gefunden.")
        }
    }

    if (sender == target.uuid) {
        return buildText {
            appendErrorPrefix()
            error("Du kannst dir keine Freundschaftsanfrage selbst senden.")
        }
    }

    val result = SurfFriendsApi.sendFriendRequest(sender, target.uuid)

    return failureMessage(result, target)
}

/** The message explaining why [result] sent no request to [target], or `null` on success. */
private fun failureMessage(result: FriendRequestCreateResult, target: SurfPlayer): Component? = when (result) {
    is FriendRequestCreateResult.Success -> null
    is FriendRequestCreateResult.AlreadyFriends -> alreadyFriendsMessage(target)
    is FriendRequestCreateResult.AlreadySentFriendRequest -> alreadySentMessage(target)
    is FriendRequestCreateResult.AlreadyReceivedFriendRequest -> alreadyReceivedMessage(target)
    is FriendRequestCreateResult.OnCooldown -> onCooldownMessage(target, result.remaining)
    is FriendRequestCreateResult.Blocked -> undeliverableMessage()
}

private fun alreadyFriendsMessage(target: SurfPlayer) = buildText {
    appendErrorPrefix()
    error("Du bist bereits mit ")
    append(target.displayName())
    error(" befreundet.")
}

private fun alreadySentMessage(target: SurfPlayer) = buildText {
    appendErrorPrefix()
    error("Du hast bereits eine Freundschaftsanfrage an ")
    append(target.displayName())
    error(" gesendet.")
}

private fun alreadyReceivedMessage(target: SurfPlayer) = buildText {
    appendErrorPrefix()
    error("Du hast bereits eine Freundschaftsanfrage von ")
    append(target.displayName())
    error(" erhalten. Möchtest du diese annehmen?")
    append {
        clickRunsCommand("/friend accept ${target.username}")
        spacer(" [")
        info("Akzeptieren".toSmallCaps())
        spacer("]")
    }
}

private fun onCooldownMessage(target: SurfPlayer, remaining: Duration) = buildText {
    appendErrorPrefix()
    error("Du kannst ")
    append(target.displayName())
    error(" erst in ")
    append(remaining.formatRemainingComponent())
    error(" wieder eine Freundschaftsanfrage senden.")
}
