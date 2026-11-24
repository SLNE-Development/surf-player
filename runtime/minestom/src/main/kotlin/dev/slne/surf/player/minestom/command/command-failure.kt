package dev.slne.surf.player.minestom.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.IntegerArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend as surfAnyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend as surfPlayerExecutorSuspend
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.api.minestom.command.executor.CommandExecutable
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.player.core.client.command.CommandHelp
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.INTERACTIVE_RPC_TIMEOUT
import dev.slne.surf.player.core.client.command.Toggle
import dev.slne.surf.player.core.client.command.runServiceCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.time.Duration
import kotlin.time.Duration as KotlinDuration

/**
 * Aborts the running command and shows [message] to its sender as an error.
 */
fun failCommand(message: String): Nothing =
    CommandAPI.failWithMessage(buildText {
        appendErrorPrefix()
        error(message)
    })

/**
 * Runs [block], which calls the microservice, waiting at most [timeout] (no limit when `null`); if
 * the microservice cannot be reached the command fails with [CommandMessages.SERVICE_UNAVAILABLE].
 */
suspend fun <T> serviceCall(
    timeout: KotlinDuration? = INTERACTIVE_RPC_TIMEOUT,
    block: suspend CoroutineScope.() -> T,
): T = runServiceCall(timeout, { failCommand(CommandMessages.SERVICE_UNAVAILABLE) }, block)

/**
 * surf-api's `anyExecutorSuspend` with the executor run as a [serviceCall]: every suspending
 * command of surf-player reports an unreachable microservice the same way, after [timeout].
 */
fun <SELF : CommandExecutable<SELF>> SELF.anyExecutorSuspend(
    timeout: KotlinDuration? = INTERACTIVE_RPC_TIMEOUT,
    executor: suspend CoroutineScope.(CommandSender, CommandArguments) -> Unit,
): SELF = surfAnyExecutorSuspend { sender, arguments ->
    serviceCall(timeout) { executor(sender, arguments) }
}

/**
 * surf-api's `playerExecutorSuspend` with the executor run as a [serviceCall], see
 * [anyExecutorSuspend].
 */
fun <SELF : CommandExecutable<SELF>> SELF.playerExecutorSuspend(
    timeout: KotlinDuration? = INTERACTIVE_RPC_TIMEOUT,
    executor: suspend CoroutineScope.(Player, CommandArguments) -> Unit,
): SELF = surfPlayerExecutorSuspend { player, arguments ->
    serviceCall(timeout) { executor(player, arguments) }
}

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
    anyExecutor { sender, _ -> sender.sendMessage(help.render { sender.hasPermission(it) }) }
}

/**
 * A click event that launches [onClick] when a player clicks it within [lifetime].
 */
fun playerClickEvent(
    lifetime: Duration = ClickCallback.DEFAULT_LIFETIME,
    onClick: suspend CoroutineScope.(Player) -> Unit
): ClickEvent<*> = ClickEvent.callback(
    ClickCallback.widen({ clicked ->
        minestomAsyncScope.launch { onClick(clicked) }
    }, Player::class.java)
) { it.lifetime(lifetime) }

/** How long the confirmation buttons of destructive commands stay valid. */
val CONFIRMATION_LIFETIME: Duration = Duration.ofMinutes(1)

/**
 * The `[an|aus]` argument, see [Toggle].
 */
class ToggleArgument(nodeName: String) : CustomArgument<Boolean, String>(StringArgument(nodeName), { info ->
    Toggle.parse(info.currentInput) ?: failCommand(Toggle.INVALID)
}) {
    init {
        replaceSuggestions(ArgumentSuggestions.strings(Toggle.suggestions))
    }
}

fun CommandAPICommand.toggleArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(ToggleArgument(nodeName).setOptional(optional = optional))

/** The node name of every page argument. */
const val PAGE_ARGUMENT = "seite"

/** The optional `[seite]` argument of every list. */
fun CommandAPICommand.pageArgument(): CommandAPICommand =
    withArguments(IntegerArgument(PAGE_ARGUMENT, min = 1).setOptional(optional = true))

/** The page the sender asked for, the first one by default. */
val CommandArguments.page: Int get() = getOptional<Int>(PAGE_ARGUMENT) ?: 1
