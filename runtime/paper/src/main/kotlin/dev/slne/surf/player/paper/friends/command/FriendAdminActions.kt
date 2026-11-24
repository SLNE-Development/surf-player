package dev.slne.surf.player.paper.friends.command

import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.player.paper.command.anyExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.friends.command.FriendAdminCommands
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.command.failCommand
import dev.slne.surf.player.paper.command.reply
import dev.slne.surf.player.paper.command.visibleOfflinePlayerArgument

/**
 * The actions of `/surfplayer friends`.
 */
fun friendAdminActions() = listOf(
    PaperAdminAction(
        "cooldown",
        FriendPermissions.ADMIN_COOLDOWN,
        FriendAdminCommands.COOLDOWN_CLEAR_DESCRIPTION,
        FriendAdminCommands.COOLDOWN_CLEAR_ARGUMENTS
    ) {
        subcommand("clear") {
            visibleOfflinePlayerArgument("spieler")
            visibleOfflinePlayerArgument("ziel", optional = true)

            anyExecutorSuspend { sender, args ->
                val player = args.awaitingOrNull<SurfPlayer?>("spieler") ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                val hasTarget = args.getOptional("ziel").isPresent
                val target = if (hasTarget) {
                    args.awaitingOrNull<SurfPlayer?>("ziel") ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                } else {
                    null
                }

                sender.reply(FriendAdminCommands.clearCooldowns(player.uuid, player.username, target?.uuid, target?.username))
            }
        }
    },
)
