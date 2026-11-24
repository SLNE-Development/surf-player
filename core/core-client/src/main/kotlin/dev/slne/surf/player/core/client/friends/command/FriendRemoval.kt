package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.api.friends.result.FriendshipRemoveResult
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.friends.util.resolveUsername
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import java.util.*

/**
 * Asks [player] to confirm ending the friendship with [friend]; [click] ends it.
 */
suspend fun friendRemoveConfirmation(friend: UUID, click: ClickEvent<*>): Component {
    val friendName = resolveUsername(friend) ?: friend.toString()

    return CommandMessages.confirmation(
        buildText {
            warning("Möchtest du die Freundschaft mit ")
            variableValue(friendName)
            warning(" wirklich beenden?")
        },
        "Um wieder befreundet zu sein, braucht ihr eine neue Freundschaftsanfrage.",
        click
    )
}

/**
 * Ends the friendship of [clicker] with [friend] once [clicker] confirmed it.
 */
suspend fun handleFriendRemoveClick(clicked: Audience, clicker: UUID, friend: UUID) {
    val result = SurfCommunityPlayer(clicker).removeFriendship(friend)

    if (result is FriendshipRemoveResult.NotFriends) {
        clicked.sendText {
            appendErrorPrefix()
            error("Du bist nicht mehr mit diesem Spieler befreundet.")
        }
    }
}
