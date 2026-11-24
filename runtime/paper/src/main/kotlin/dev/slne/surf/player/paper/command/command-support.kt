package dev.slne.surf.player.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandAPIPaper
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.core.client.command.CommandHelp
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.Toggle
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Duration

/**
 * Aborts the running command and shows [message] to its sender as an error.
 */
fun failCommand(message: String): Nothing =
    throw CommandAPIPaper.failWithAdventureComponent(buildText {
        appendErrorPrefix()
        error(message)
    })

/**
 * Hands the [outcome] of a shared command to this sender: a reply is sent, a failure aborts the
 * command.
 */
fun CommandSender.reply(outcome: CommandOutcome) {
    when (outcome) {
        is CommandOutcome.Reply -> sendMessage(outcome.message)
        is CommandOutcome.Failure -> failCommand(outcome.message)
        CommandOutcome.Silent -> Unit
    }
}

/**
 * Shows [help] when the command is run without arguments.
 */
fun CommandAPICommand.helpExecutor(help: CommandHelp): CommandAPICommand = apply {
    anyExecutor { sender, _ -> sender.sendMessage(help.render(sender::hasPermission)) }
}

/**
 * A click event that launches [onClick] when a player clicks it within [lifetime].
 */
fun playerClickEvent(
    lifetime: Duration = ClickCallback.DEFAULT_LIFETIME,
    onClick: suspend CoroutineScope.(Player) -> Unit
): ClickEvent<*> = ClickEvent.callback(
    ClickCallback.widen({ clicked ->
        plugin.launch { onClick(clicked) }
    }, Player::class.java)
) { it.lifetime(lifetime) }

/** How long the confirmation buttons of destructive commands stay valid. */
val CONFIRMATION_LIFETIME: Duration = Duration.ofMinutes(1)

/**
 * The `[an|aus]` argument, see [Toggle].
 */
class ToggleArgument(nodeName: String) : CustomArgument<Boolean, String>(StringArgument(nodeName), { info ->
    Toggle.parse(info.input()) ?: throw CustomArgumentException.fromAdventureComponent(buildText {
        appendErrorPrefix()
        error(Toggle.INVALID)
    })
}) {
    init {
        replaceSuggestions(ArgumentSuggestions.strings(Toggle.suggestions))
    }
}

fun CommandAPICommand.toggleArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(ToggleArgument(nodeName).setOptional(optional))

/** The node name of every page argument. */
const val PAGE_ARGUMENT = "seite"

/** The optional `[seite]` argument of every list. */
fun CommandAPICommand.pageArgument(): CommandAPICommand =
    withArguments(IntegerArgument(PAGE_ARGUMENT, 1).setOptional(true))

/** The page the sender asked for, the first one by default. */
val CommandArguments.page: Int get() = getOrDefaultUnchecked(PAGE_ARGUMENT, 1)
