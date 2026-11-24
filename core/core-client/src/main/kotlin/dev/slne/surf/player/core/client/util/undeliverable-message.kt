package dev.slne.surf.player.core.client.util

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component

/**
 * The neutral error shown when something a player sent did not reach its receiver, e.g. a direct
 * message or a friend request to a player who ignores the sender.
 *
 * Deliberately says nothing about the reason, so the sender cannot find out whether they are
 * ignored. Every feature that rejects for such a reason must show exactly this message.
 */
fun undeliverableMessage(): Component = buildText {
    appendErrorPrefix()
    error("Deine Nachricht konnte nicht zugestellt werden.")
}
