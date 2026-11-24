package dev.slne.surf.player.velocity.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.api.velocity.command.executors.anyExecutorSuspend
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.velocity.command.argument.surfBackendServerArgument
import dev.slne.surf.player.api.queue.SurfQueueApi
import dev.slne.surf.player.core.client.clan.command.ClanCommands
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.queue.command.QueueMessages
import dev.slne.surf.player.core.client.queue.permission.QueuePermissions
import dev.slne.surf.player.core.client.settings.command.SettingsCommandMessages
import dev.slne.surf.player.core.client.settings.permission.SettingsPermissions
import dev.slne.surf.player.core.client.settings.service.SettingsService
import dev.slne.surf.player.velocity.queue.config.VelocityQueueConfig
import net.kyori.adventure.text.Component
import kotlin.system.measureTimeMillis

/** The actions of `/surfplayervelocity clan`. */
fun clanAdminActions() = listOf(
    VelocityAdminAction("reload", ClanPermissions.ADMIN_RELOAD, "Lädt die Clan-Konfiguration des Proxys neu") {
        anyExecutor { sender, _ -> sender.reply(ClanCommands.reload()) }
    },
)

/** The actions of `/surfplayervelocity settings`. */
fun settingsAdminActions() = listOf(
    VelocityAdminAction("refresh", SettingsPermissions.ADMIN_REFRESH, "Lädt alle Einstellungen auf dem Proxy neu") {
        anyExecutorSuspend { sender, _ ->
            val millis = measureTimeMillis { SettingsService.refreshSettings() }
            sender.sendMessage(SettingsCommandMessages.refreshed(SettingsService.settings.size, millis))
        }
    },
    VelocityAdminAction("list", SettingsPermissions.ADMIN_LIST, "Zeigt alle verfügbaren Einstellungen") {
        anyExecutor { sender, _ ->
            if (SettingsService.settings.isEmpty()) {
                sender.sendMessage(SettingsCommandMessages.noSettings)
            } else {
                sender.sendMessage(SettingsCommandMessages.list(SettingsService.settings))
            }
        }
    },
)

/** An action on the queue of the backend server given as `<server>`. */
private fun queueServerAction(
    name: String,
    permission: String,
    description: String,
    run: suspend (SurfServer) -> Component,
) = VelocityAdminAction(name, permission, description, "<server>") {
    serverArgument()
    anyExecutorSuspend { sender, arguments ->
        val server: SurfServer by arguments
        sender.sendMessage(run(server))
    }
}

private fun CommandAPICommand.serverArgument() = surfBackendServerArgument("server")

/** The actions of `/surfplayervelocity queue`. */
fun queueAdminActions() = listOf(
    VelocityAdminAction("reload", QueuePermissions.ADMIN_RELOAD, "Lädt die Warteschlangen-Konfiguration des Proxys neu") {
        anyExecutor { sender, _ ->
            val millis = measureTimeMillis { VelocityQueueConfig.reloadFromFile() }
            sender.sendMessage(CommandMessages.configReloaded("queue", millis))
        }
    },
    queueServerAction("pause", QueuePermissions.ADMIN_PAUSE, "Pausiert eine Warteschlange") { server ->
        SurfQueueApi.queue(server).pause()
        QueueMessages.queuePaused(server.name)
    },
    queueServerAction("resume", QueuePermissions.ADMIN_RESUME, "Setzt eine Warteschlange fort") { server ->
        SurfQueueApi.queue(server).resume()
        QueueMessages.queueResumed(server.name)
    },
    queueServerAction("status", QueuePermissions.ADMIN_STATUS, "Zeigt, ob eine Warteschlange pausiert ist") { server ->
        QueueMessages.queuePauseStatus(server.name, SurfQueueApi.queue(server).isPaused())
    },
)
