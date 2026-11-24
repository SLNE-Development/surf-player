package dev.slne.surf.player.minestom.command

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.player.core.client.command.AdminActionHelp
import dev.slne.surf.player.core.client.command.AdminCommandHelp
import dev.slne.surf.player.core.client.command.AdminPermissions
import dev.slne.surf.player.minestom.MinestomPlayerFeature

/**
 * One action of a feature below `/surfplayer <feature>`: `/surfplayer <feature> <name> <arguments>`.
 *
 * [configure] adds the arguments and executors to the action's subcommand; the subcommand already
 * requires [permission].
 */
class MinestomAdminAction(
    val name: String,
    val permission: String,
    val description: String,
    val arguments: String = "",
    val configure: CommandAPICommand.() -> Unit,
) {
    val help get() = AdminActionHelp(name, arguments, description, permission)
}

/**
 * The single admin command of the Minestom plugin, `/surfplayer`. Each feature contributes its
 * actions through [MinestomPlayerFeature.adminActions].
 */
object MinestomAdminCommand {
    const val NAME = "surfplayer"

    fun register(features: List<MinestomPlayerFeature>) {
        val subtrees = features.associate { it.id to it.adminActions() }.filterValues { it.isNotEmpty() }
        val help = subtrees.mapValues { (_, actions) -> actions.map { it.help } }

        commandAPICommand(NAME) {
            withPermission(AdminPermissions.ADMIN)

            anyExecutor { sender, _ ->
                sender.sendMessage(AdminCommandHelp.overview(NAME, help) { sender.hasPermission(it) })
            }

            for ((feature, actions) in subtrees) {
                withSubcommand(subcommand(feature) {
                    withRequirement { sender -> actions.any { sender.hasPermission(it.permission) } }

                    anyExecutor { sender, _ ->
                        sender.sendMessage(
                            AdminCommandHelp.feature(NAME, feature, help.getValue(feature)) { sender.hasPermission(it) }
                        )
                    }

                    for (action in actions) {
                        withSubcommand(subcommand(action.name) {
                            withPermission(action.permission)
                            action.configure(this)
                        })
                    }
                })
            }
        }
    }
}
