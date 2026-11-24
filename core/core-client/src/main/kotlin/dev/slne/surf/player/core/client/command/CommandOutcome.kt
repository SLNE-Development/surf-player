package dev.slne.surf.player.core.client.command

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component

/**
 * What a command shared by every platform produced: a message for the sender, a failure that aborts
 * the command (the platform throws its command failure with [Failure.message]), or nothing.
 */
sealed interface CommandOutcome {
    data class Reply(val message: Component) : CommandOutcome
    data class Failure(val message: String) : CommandOutcome
    data object Silent : CommandOutcome
}

fun reply(message: Component): CommandOutcome = CommandOutcome.Reply(message)

fun failure(message: String): CommandOutcome = CommandOutcome.Failure(message)

/**
 * A neutral reply with the info prefix (command conventions §4), for results that are not an
 * error, such as "nothing found" or an empty list.
 */
fun notice(message: String): CommandOutcome = reply(noticeMessage(message))

/** The message of [notice]. */
fun noticeMessage(message: String): Component = buildText {
    appendInfoPrefix()
    info(message)
}
