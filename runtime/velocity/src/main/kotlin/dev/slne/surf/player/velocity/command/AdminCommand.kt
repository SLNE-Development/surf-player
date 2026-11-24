package dev.slne.surf.player.velocity.command

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.VelocityBrigadierMessage
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.VelocityExecutable
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.velocity.command.executors.anyExecutorSuspend as surfAnyExecutorSuspend
import dev.slne.surf.api.velocity.command.executors.playerExecutorSuspend as surfPlayerExecutorSuspend
import dev.slne.surf.player.core.client.command.AdminActionHelp
import dev.slne.surf.player.core.client.command.AdminCommandHelp
import dev.slne.surf.player.core.client.command.AdminPermissions
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.INTERACTIVE_RPC_TIMEOUT
import dev.slne.surf.player.core.client.command.runServiceCall
import dev.slne.surf.player.velocity.VelocityPlayerFeature
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

/**
 * Aborts the running command and shows [message] to its sender as an error.
 */
fun failCommand(message: String): Nothing =
    throw CommandAPI.failWithMessage(VelocityBrigadierMessage.tooltip(buildText {
        appendErrorPrefix()
        error(message)
    }))

/**
 * Runs [block], which calls the microservice, waiting at most [timeout] (no limit when `null`); if
 * the microservice cannot be reached the command fails with [CommandMessages.SERVICE_UNAVAILABLE].
 */
suspend fun <T> serviceCall(
    timeout: Duration? = INTERACTIVE_RPC_TIMEOUT,
    block: suspend CoroutineScope.() -> T,
): T = runServiceCall(timeout, { failCommand(CommandMessages.SERVICE_UNAVAILABLE) }, block)

/**
 * surf-api's `anyExecutorSuspend` with the executor run as a [serviceCall]: every suspending
 * command of surf-player reports an unreachable microservice the same way, after [timeout].
 */
fun VelocityExecutable<*>.anyExecutorSuspend(
    timeout: Duration? = INTERACTIVE_RPC_TIMEOUT,
    executor: suspend CoroutineScope.(CommandSource, CommandArguments) -> Unit,
) = surfAnyExecutorSuspend { sender, arguments ->
    serviceCall(timeout) { executor(sender, arguments) }
}

/**
 * surf-api's `playerExecutorSuspend` with the executor run as a [serviceCall], see
 * [anyExecutorSuspend].
 */
fun VelocityExecutable<*>.playerExecutorSuspend(
    timeout: Duration? = INTERACTIVE_RPC_TIMEOUT,
    executor: suspend CoroutineScope.(Player, CommandArguments) -> Unit,
) = surfPlayerExecutorSuspend { player, arguments ->
    serviceCall(timeout) { executor(player, arguments) }
}

/**
 * Hands the [outcome] of a shared command to this sender: a reply is sent, a failure aborts the
 * command.
 */
fun CommandSource.reply(outcome: CommandOutcome) {
    when (outcome) {
        is CommandOutcome.Reply -> sendMessage(outcome.message)
        is CommandOutcome.Failure -> failCommand(outcome.message)
        CommandOutcome.Silent -> Unit
    }
}

/**
 * One action of a feature below `/surfplayervelocity <feature>`.
 *
 * [configure] adds the arguments and executors to the action's subcommand; the subcommand already
 * requires [permission].
 */
class VelocityAdminAction(
    val name: String,
    val permission: String,
    val description: String,
    val arguments: String = "",
    val configure: CommandAPICommand.() -> Unit,
) {
    val help get() = AdminActionHelp(name, arguments, description, permission)
}

/**
 * The single admin command of the Velocity plugin, `/surfplayervelocity`. It never shares its name
 * with a backend command, since a backend command of the same name would take precedence in game.
 */
object VelocityAdminCommand {
    const val NAME = "surfplayervelocity"

    fun register(features: List<VelocityPlayerFeature>) {
        val subtrees = features.associate { it.id to it.adminActions() }.filterValues { it.isNotEmpty() }
        val help = subtrees.mapValues { (_, actions) -> actions.map { it.help } }

        commandAPICommand(NAME) {
            withPermission(AdminPermissions.ADMIN)

            anyExecutor { sender, _ ->
                sender.sendMessage(AdminCommandHelp.overview(NAME, help, sender::hasPermission))
            }

            for ((feature, actions) in subtrees) {
                subcommand(feature) {
                    withRequirement { sender -> actions.any { sender.hasPermission(it.permission) } }

                    anyExecutor { sender, _ ->
                        sender.sendMessage(
                            AdminCommandHelp.feature(NAME, feature, help.getValue(feature), sender::hasPermission)
                        )
                    }

                    for (action in actions) {
                        subcommand(action.name) {
                            withPermission(action.permission)
                            action.configure(this)
                        }
                    }
                }
            }
        }
    }
}
