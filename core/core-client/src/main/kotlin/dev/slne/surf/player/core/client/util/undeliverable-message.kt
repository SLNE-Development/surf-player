package dev.slne.surf.player.core.client.util

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component

/**
 * The neutral error shown when a message a player sent did not reach its receiver, e.g. a direct
 * message to a player who ignores the sender.
 *
 * Deliberately says nothing about the reason, so the sender cannot find out whether they are
 * ignored. (A friend request blocked by an ignore instead says that the target accepts no friend
 * requests, see `friendRequestsDisabledMessage`.)
 */
fun undeliverableMessage(): Component = buildText {
    appendErrorPrefix()
    error("Deine Nachricht konnte nicht zugestellt werden.")
}
