package dev.slne.surf.player.paper.queue.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.integerArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.queue.command.QueueCommands
import dev.slne.surf.player.core.client.queue.permission.QueuePermissions
import dev.slne.surf.player.core.common.queue.queue.RedisQueueScore
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.command.failCommand
import dev.slne.surf.player.paper.command.page
import dev.slne.surf.player.paper.command.pageArgument
import dev.slne.surf.player.paper.command.reply
import dev.slne.surf.player.paper.command.visibleOfflinePlayerArgument

private const val SERVER = "server"
private const val PLAYER = "spieler"
private const val PRIORITY = "prioritaet"

/** The queue the action is about: the given backend server, this server by default. */
private val CommandArguments.serverName: String
    get() = (getOptional(SERVER).orElse(null) as? SurfServer)?.name ?: SurfServer.current().name

private suspend fun CommandArguments.player(): SurfPlayer =
    awaitingOrNull<SurfPlayer?>(PLAYER) ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)

private fun CommandAPICommand.serverArgument() = surfBackendServerArgument(SERVER, optional = true)

/** An action that only takes the optional `[server]` argument. */
private fun serverAction(
    name: String,
    permission: String,
    description: String,
    run: suspend (serverName: String) -> CommandOutcome,
) = PaperAdminAction(name, permission, description, "[server]") {
    serverArgument()
    anyExecutorSuspend { sender, args -> sender.reply(run(args.serverName)) }
}

/**
 * The actions of `/surfplayer queue`.
 */
fun queueAdminActions() = listOf(
    PaperAdminAction("reload", QueuePermissions.ADMIN_RELOAD, "Lädt die Warteschlangen-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.reply(QueueCommands.reload()) }
    },

    PaperAdminAction("list", QueuePermissions.ADMIN_LIST, "Zeigt die Spieler einer Warteschlange", "[server] [seite]") {
        serverArgument()
        pageArgument()
        anyExecutorSuspend { sender, args -> sender.reply(QueueCommands.list(args.serverName, args.page)) }
    },

    PaperAdminAction("info", QueuePermissions.ADMIN_INFO, "Zeigt den Eintrag eines Spielers", "<spieler> [server]") {
        visibleOfflinePlayerArgument(PLAYER)
        serverArgument()
        anyExecutorSuspend { sender, args ->
            val player = args.player()
            sender.reply(QueueCommands.info(player.uuid, player.lastKnownName ?: "${player.uuid}", args.serverName))
        }
    },

    PaperAdminAction(
        "enqueue",
        QueuePermissions.ADMIN_ENQUEUE,
        "Reiht einen Spieler in eine Warteschlange ein",
        "<spieler> [server] [prioritaet]"
    ) {
        visibleOfflinePlayerArgument(PLAYER)
        serverArgument()
        integerArgument(PRIORITY, min = 0, max = RedisQueueScore.MAX_PRIORITY, optional = true)
        anyExecutorSuspend { sender, args ->
            val player = args.player()
            val priority = args.getOptional(PRIORITY).orElse(null) as Int?
            sender.reply(
                QueueCommands.enqueue(player.uuid, player.lastKnownName ?: "${player.uuid}", args.serverName, priority)
            )
        }
    },

    PaperAdminAction(
        "dequeue",
        QueuePermissions.ADMIN_DEQUEUE,
        "Entfernt einen Spieler aus einer Warteschlange",
        "<spieler> [server]"
    ) {
        visibleOfflinePlayerArgument(PLAYER)
        serverArgument()
        anyExecutorSuspend { sender, args ->
            val player = args.player()
            sender.reply(QueueCommands.dequeue(player.uuid, player.lastKnownName ?: "${player.uuid}", args.serverName))
        }
    },

    serverAction("clear", QueuePermissions.ADMIN_CLEAR, "Leert eine Warteschlange", QueueCommands::clear),
    serverAction("cleanup", QueuePermissions.ADMIN_CLEANUP, "Entfernt abgelaufene Einträge", QueueCommands::cleanup),
    serverAction("fix", QueuePermissions.ADMIN_FIX, "Repariert eine hängende Warteschlange", QueueCommands::fix),
    serverAction("pause", QueuePermissions.ADMIN_PAUSE, "Pausiert eine Warteschlange", QueueCommands::pause),
    serverAction("resume", QueuePermissions.ADMIN_RESUME, "Setzt eine Warteschlange fort", QueueCommands::resume),
    serverAction("status", QueuePermissions.ADMIN_STATUS, "Zeigt, ob eine Warteschlange pausiert ist", QueueCommands::status),

    PaperAdminAction(
        "metrics",
        QueuePermissions.ADMIN_METRICS,
        "Metriken: start, stop (Protokollierung), snapshot",
        "<start|stop|snapshot>"
    ) {
        subcommand("start") { anyExecutor { sender, _ -> sender.reply(QueueCommands.startMetrics()) } }
        subcommand("stop") { anyExecutor { sender, _ -> sender.reply(QueueCommands.stopMetrics()) } }
        subcommand("snapshot") { anyExecutorSuspend { sender, _ -> QueueCommands.metricsSnapshot(sender) } }
    },
)
