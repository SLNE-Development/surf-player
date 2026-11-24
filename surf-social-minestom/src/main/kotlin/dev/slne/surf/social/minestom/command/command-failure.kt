package dev.slne.surf.social.minestom.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.minestom.command.CommandAPI

/**
 * Aborts the running command and shows [message] to its sender as an error.
 */
fun failCommand(message: String): Nothing =
    CommandAPI.failWithMessage(buildText {
        appendErrorPrefix()
        error(message)
    })
