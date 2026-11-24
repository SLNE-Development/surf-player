package dev.slne.surf.player.paper.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.core.client.command.AdminActionHelp
import dev.slne.surf.player.core.client.command.AdminCommandHelp
import dev.slne.surf.player.core.client.command.AdminPermissions
import dev.slne.surf.player.paper.PaperPlayerFeature

/**
 * One action of a feature below `/surfplayer <feature>`: `/surfplayer <feature> <name> <arguments>`.
 *
 * [configure] adds the arguments and executors to the action's subcommand; the subcommand already
 * requires [permission].
 */
class PaperAdminAction(
    val name: String,
    val permission: String,
    val description: String,
    val arguments: String = "",
    val configure: CommandAPICommand.() -> Unit,
) {
    val help get() = AdminActionHelp(name, arguments, description, permission)
}

/**
 * The single admin command of the Paper plugin, `/surfplayer`. Each feature contributes its actions
 * through [PaperPlayerFeature.adminActions].
 */
object PaperAdminCommand {
    const val NAME = "surfplayer"

    fun register(features: List<PaperPlayerFeature>) {
        val subtrees = features.associate { it.name to it.adminActions() }.filterValues { it.isNotEmpty() }
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
