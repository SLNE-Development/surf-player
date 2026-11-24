package dev.slne.surf.player.velocity.command

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.VelocityBrigadierMessage
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.core.client.command.AdminActionHelp
import dev.slne.surf.player.core.client.command.AdminCommandHelp
import dev.slne.surf.player.core.client.command.AdminPermissions
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.velocity.VelocityPlayerFeature

/**
 * Aborts the running command and shows [message] to its sender as an error.
 */
fun failCommand(message: String): Nothing =
    throw CommandAPI.failWithMessage(VelocityBrigadierMessage.tooltip(buildText {
        appendErrorPrefix()
        error(message)
    }))

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
