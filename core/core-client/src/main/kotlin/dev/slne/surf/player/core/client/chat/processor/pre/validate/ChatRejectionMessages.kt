package dev.slne.surf.player.core.client.chat.processor.pre.validate

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.api.chat.message.MessageValidationError
import net.kyori.adventure.text.Component

/**
 * What the sender sees when their chat message is rejected. Every rejection is a failure, so every
 * one uses the error prefix, like every other failure (docs/command-conventions.md).
 */
object ChatRejectionMessages {
    val DISALLOWED_LINK: Component = rejection(Component.text("Deine Nachricht enthält einen unerlaubten Link!"))

    val ILLEGAL_CHARACTERS: Component = rejection(Component.text("Deine Nachricht enthält unerlaubte Zeichen."))

    fun validationFailed(error: MessageValidationError): Component = rejection(error.errorMessage)

    private fun rejection(reason: Component): Component = buildText {
        appendErrorPrefix()
        append(reason.colorIfAbsent(Colors.ERROR))
    }
}
