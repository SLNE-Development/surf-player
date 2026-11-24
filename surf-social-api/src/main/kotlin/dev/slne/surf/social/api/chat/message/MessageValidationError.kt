package dev.slne.surf.social.api.chat.message

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component

private val EMPTY_CONTENT_MESSAGE = buildText { error("Deine Nachricht darf nicht leer sein.") }
private val TOO_MANY_CAPS_MESSAGE = buildText { error("Deine Nachricht enthält zu viele Großbuchstaben.") }
private val AUTO_DISABLED_MESSAGE = buildText { error("Du kannst zurzeit nicht schreiben.") }
private val CHAT_DISABLED_MESSAGE = buildText { error("Der Chat ist vorübergehend deaktiviert.") }

/**
 * Why a message failed validation.
 *
 * @property errorMessage the message shown to the sender
 * @property name a short, human-readable name of the error
 */
sealed class MessageValidationError(val errorMessage: Component, val name: String) {
    class EmptyContent : MessageValidationError(EMPTY_CONTENT_MESSAGE, "Kein Inhalt")

    class TooManyCaps : MessageValidationError(TOO_MANY_CAPS_MESSAGE, "Zu viele Großbuchstaben")

    /** Writing is disabled automatically, for example because too many players are online. */
    class AutoDisabled : MessageValidationError(AUTO_DISABLED_MESSAGE, "Zu viele Spieler")

    /** The chat is disabled on this server. */
    class ChatDisabled : MessageValidationError(CHAT_DISABLED_MESSAGE, "Chat deaktiviert")
}
