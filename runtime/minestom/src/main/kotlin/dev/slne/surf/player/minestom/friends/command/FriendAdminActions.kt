package dev.slne.surf.player.minestom.friends.command

import dev.slne.surf.player.minestom.command.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.friends.command.FriendAdminCommands
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.minestom.command.MinestomAdminAction
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.player.minestom.command.reply
import dev.slne.surf.player.minestom.command.visibleOfflinePlayerArgument
import kotlinx.coroutines.Deferred

/**
 * The actions of `/surfplayer friends`.
 */
fun friendAdminActions() = listOf(
    MinestomAdminAction(
        "cooldown",
        FriendPermissions.ADMIN_COOLDOWN,
        FriendAdminCommands.COOLDOWN_CLEAR_DESCRIPTION,
        FriendAdminCommands.COOLDOWN_CLEAR_ARGUMENTS
    ) {
        withSubcommand(subcommand("clear") {
            visibleOfflinePlayerArgument("spieler")
            visibleOfflinePlayerArgument("ziel", optional = true)

            anyExecutorSuspend { sender, args ->
                val player = args.get<Deferred<SurfPlayer?>>("spieler").await()
                    ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                val target = args.getOptional<Deferred<SurfPlayer?>>("ziel")?.let { deferred ->
                    deferred.await() ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                }

                sender.reply(FriendAdminCommands.clearCooldowns(player.uuid, player.username, target?.uuid, target?.username))
            }
        })
    },
)
