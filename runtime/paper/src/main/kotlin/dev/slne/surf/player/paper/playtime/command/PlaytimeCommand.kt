package dev.slne.surf.player.paper.playtime.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.longArgument
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.playtime.command.PlaytimeCommands
import dev.slne.surf.player.core.client.playtime.permission.PlaytimePermissions
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.command.VisibleOfflinePlayerArgument
import dev.slne.surf.player.paper.command.reply

private const val PLAYER = "spieler"

/**
 * `/playtime [spieler]` (`/pt`). Other players need [PlaytimePermissions.OTHERS].
 */
fun playtimeCommand() = commandAPICommand("playtime") {
    withAliases("pt")
    withPermission(PlaytimePermissions.COMMAND_PLAYTIME)
    withOptionalArguments(VisibleOfflinePlayerArgument(PLAYER).withPermission(PlaytimePermissions.OTHERS))

    playerExecutorSuspend { player, args ->
        if (args.getOptional(PLAYER).isEmpty) {
            PlaytimeCommands.showOwn(player, player.uniqueId)
        } else {
            player.reply(PlaytimeCommands.showOther(player, args.awaitingOrNull<SurfPlayer?>(PLAYER)))
        }
    }
}

/**
 * The actions of `/surfplayer playtime`.
 */
fun playtimeAdminActions() = listOf(
    PaperAdminAction("reload", PlaytimePermissions.ADMIN_RELOAD, "Lädt die Playtime-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.reply(PlaytimeCommands.reload()) }
    },

    PaperAdminAction(
        "recalculate",
        PlaytimePermissions.ADMIN_RECALCULATE,
        "Berechnet die Login-Streak eines Spielers (ohne Spieler: aller Spieler) neu",
        "[spieler]"
    ) {
        withOptionalArguments(VisibleOfflinePlayerArgument(PLAYER))
        anyExecutorSuspend { sender, args ->
            val all = args.getOptional(PLAYER).isEmpty
            sender.reply(PlaytimeCommands.recalculate(sender, args.awaitingOrNull<SurfPlayer?>(PLAYER), all))
        }
    },

    PaperAdminAction(
        "pause",
        PlaytimePermissions.ADMIN_PAUSE,
        "Verwaltet Streak-Pausen: create <von> <bis>, delete <id>, list",
        "<create|delete|list>"
    ) {
        subcommand("create") {
            stringArgument("von")
            stringArgument("bis")
            anyExecutorSuspend { sender, args ->
                val von: String by args
                val bis: String by args
                sender.reply(PlaytimeCommands.createPause(sender, von, bis))
            }
        }

        subcommand("delete") {
            longArgument("id")
            anyExecutorSuspend { sender, args ->
                val id: Long by args
                sender.reply(PlaytimeCommands.deletePause(sender, id))
            }
        }

        subcommand("list") {
            anyExecutorSuspend { sender, _ -> sender.reply(PlaytimeCommands.listPauses(sender)) }
        }
    },
)
