package dev.slne.surf.player.minestom.playtime.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.longArgument
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.stringArgument
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.SurfOfflinePlayerArgument
import dev.slne.surf.player.core.client.playtime.command.PlaytimeCommands
import dev.slne.surf.player.core.client.playtime.permission.PlaytimePermissions
import dev.slne.surf.player.minestom.command.MinestomAdminAction
import dev.slne.surf.player.minestom.command.reply
import kotlinx.coroutines.Deferred

private const val PLAYER = "spieler"

private suspend fun CommandArguments.optionalPlayer(): SurfPlayer? =
    if (PLAYER in this) get<Deferred<SurfPlayer?>>(PLAYER).await() else null

/**
 * `/playtime [spieler]` (`/pt`). Other players need [PlaytimePermissions.OTHERS].
 */
fun playtimeCommand() = commandAPICommand("playtime") {
    withAliases("pt")
    withPermission(PlaytimePermissions.COMMAND_PLAYTIME)
    withOptionalArguments(SurfOfflinePlayerArgument(PLAYER).withPermission(PlaytimePermissions.OTHERS))

    playerExecutorSuspend { player, args ->
        if (PLAYER !in args) {
            PlaytimeCommands.showOwn(player, player.uuid)
        } else {
            player.reply(PlaytimeCommands.showOther(player, args.optionalPlayer()))
        }
    }
}

/**
 * The actions of `/surfplayer playtime`.
 */
fun playtimeAdminActions() = listOf(
    MinestomAdminAction("reload", PlaytimePermissions.ADMIN_RELOAD, "Lädt die Playtime-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.reply(PlaytimeCommands.reload()) }
    },

    MinestomAdminAction(
        "recalculate",
        PlaytimePermissions.ADMIN_RECALCULATE,
        "Berechnet die Login-Streak eines Spielers (ohne Spieler: aller Spieler) neu",
        "[spieler]"
    ) {
        withOptionalArguments(SurfOfflinePlayerArgument(PLAYER))
        anyExecutorSuspend { sender, args ->
            sender.reply(PlaytimeCommands.recalculate(sender, args.optionalPlayer(), all = PLAYER !in args))
        }
    },

    MinestomAdminAction(
        "pause",
        PlaytimePermissions.ADMIN_PAUSE,
        "Verwaltet Streak-Pausen: create <von> <bis>, delete <id>, list",
        "<create|delete|list>"
    ) {
        withSubcommand(subcommand("create") {
            stringArgument("von")
            stringArgument("bis")
            anyExecutorSuspend { sender, args ->
                val von: String by args
                val bis: String by args
                sender.reply(PlaytimeCommands.createPause(sender, von, bis))
            }
        })

        withSubcommand(subcommand("delete") {
            longArgument("id")
            anyExecutorSuspend { sender, args ->
                val id: Long by args
                sender.reply(PlaytimeCommands.deletePause(sender, id))
            }
        })

        withSubcommand(subcommand("list") {
            anyExecutorSuspend { sender, _ -> sender.reply(PlaytimeCommands.listPauses(sender)) }
        })
    },
)
