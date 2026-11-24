package dev.slne.surf.player.minestom.queue.command

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.integerArgument
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfBackendServerArgument
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.queue.command.QueueCommands
import dev.slne.surf.player.core.client.queue.permission.QueuePermissions
import dev.slne.surf.player.core.common.queue.queue.RedisQueueScore
import dev.slne.surf.player.minestom.command.MinestomAdminAction
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.player.minestom.command.page
import dev.slne.surf.player.minestom.command.pageArgument
import dev.slne.surf.player.minestom.command.reply
import kotlinx.coroutines.Deferred

private const val SERVER = "server"
private const val PLAYER = "spieler"
private const val PRIORITY = "prioritaet"

/** The queue the action is about: the given backend server, this server by default. */
private val CommandArguments.serverName: String
    get() = getOptional<SurfServer>(SERVER)?.name ?: SurfServer.current().name

private suspend fun CommandArguments.player(): SurfPlayer =
    get<Deferred<SurfPlayer?>>(PLAYER).await() ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)

private val SurfPlayer.displayedName get() = lastKnownName ?: uuid.toString()

private fun CommandAPICommand.serverArgument() = surfBackendServerArgument(SERVER, optional = true)

/** An action that only takes the optional `[server]` argument. */
private fun serverAction(
    name: String,
    permission: String,
    description: String,
    run: suspend (serverName: String) -> CommandOutcome,
) = MinestomAdminAction(name, permission, description, "[server]") {
    serverArgument()
    anyExecutorSuspend { sender, args -> sender.reply(run(args.serverName)) }
}

/**
 * The actions of `/surfplayer queue`.
 */
fun queueAdminActions() = listOf(
    MinestomAdminAction("reload", QueuePermissions.ADMIN_RELOAD, "Lädt die Warteschlangen-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.reply(QueueCommands.reload()) }
    },

    MinestomAdminAction("list", QueuePermissions.ADMIN_LIST, "Zeigt die Spieler einer Warteschlange", "[server] [seite]") {
        serverArgument()
        pageArgument()
        anyExecutorSuspend { sender, args -> sender.reply(QueueCommands.list(args.serverName, args.page)) }
    },

    MinestomAdminAction("info", QueuePermissions.ADMIN_INFO, "Zeigt den Eintrag eines Spielers", "<spieler> [server]") {
        surfOfflinePlayerArgument(PLAYER)
        serverArgument()
        anyExecutorSuspend { sender, args ->
            val player = args.player()
            sender.reply(QueueCommands.info(player.uuid, player.displayedName, args.serverName))
        }
    },

    MinestomAdminAction(
        "enqueue",
        QueuePermissions.ADMIN_ENQUEUE,
        "Reiht einen Spieler in eine Warteschlange ein",
        "<spieler> [server] [prioritaet]"
    ) {
        surfOfflinePlayerArgument(PLAYER)
        serverArgument()
        integerArgument(PRIORITY, min = 0, max = RedisQueueScore.MAX_PRIORITY, optional = true)
        anyExecutorSuspend { sender, args ->
            val player = args.player()
            val priority = args.getOptional<Int>(PRIORITY)
            sender.reply(QueueCommands.enqueue(player.uuid, player.displayedName, args.serverName, priority))
        }
    },

    MinestomAdminAction(
        "dequeue",
        QueuePermissions.ADMIN_DEQUEUE,
        "Entfernt einen Spieler aus einer Warteschlange",
        "<spieler> [server]"
    ) {
        surfOfflinePlayerArgument(PLAYER)
        serverArgument()
        anyExecutorSuspend { sender, args ->
            val player = args.player()
            sender.reply(QueueCommands.dequeue(player.uuid, player.displayedName, args.serverName))
        }
    },

    serverAction("clear", QueuePermissions.ADMIN_CLEAR, "Leert eine Warteschlange", QueueCommands::clear),
    serverAction("cleanup", QueuePermissions.ADMIN_CLEANUP, "Entfernt abgelaufene Einträge", QueueCommands::cleanup),
    serverAction("fix", QueuePermissions.ADMIN_FIX, "Repariert eine hängende Warteschlange", QueueCommands::fix),
    serverAction("pause", QueuePermissions.ADMIN_PAUSE, "Pausiert eine Warteschlange", QueueCommands::pause),
    serverAction("resume", QueuePermissions.ADMIN_RESUME, "Setzt eine Warteschlange fort", QueueCommands::resume),
    serverAction("status", QueuePermissions.ADMIN_STATUS, "Zeigt, ob eine Warteschlange pausiert ist", QueueCommands::status),

    MinestomAdminAction(
        "metrics",
        QueuePermissions.ADMIN_METRICS,
        "Metriken: start, stop (Protokollierung), snapshot",
        "<start|stop|snapshot>"
    ) {
        withSubcommand(subcommand("start") { anyExecutor { sender, _ -> sender.reply(QueueCommands.startMetrics()) } })
        withSubcommand(subcommand("stop") { anyExecutor { sender, _ -> sender.reply(QueueCommands.stopMetrics()) } })
        withSubcommand(subcommand("snapshot") {
            anyExecutorSuspend { sender, _ -> QueueCommands.metricsSnapshot(sender) }
        })
    },
)
