package dev.slne.surf.player.paper.settings.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.client.settings.command.SettingsCommandActions
import dev.slne.surf.player.core.client.settings.command.SettingsCommandMessages
import dev.slne.surf.player.core.client.settings.command.settingsCommandHelp
import dev.slne.surf.player.core.client.settings.permission.SettingsPermissions
import dev.slne.surf.player.core.client.settings.service.SettingsService
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.command.anyExecutorSuspend
import dev.slne.surf.player.paper.command.helpExecutor
import dev.slne.surf.player.paper.command.toggleArgument
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.settings.command.argument.settingArgument
import dev.slne.surf.player.paper.settings.menu.settingsView
import kotlin.system.measureTimeMillis

/**
 * `/settings` opens the menu; `/settings help|toggle|info` work without it.
 */
fun settingsCommand() = commandAPICommand("settings") {
    withPermission(SettingsPermissions.COMMAND_SETTINGS)

    playerExecutor { player, _ -> settingsView.open(player) }

    subcommand("help") {
        helpExecutor(settingsCommandHelp)
    }

    subcommand("toggle") {
        withPermission(SettingsPermissions.COMMAND_TOGGLE)
        settingArgument("einstellung")
        toggleArgument("zustand", optional = true)

        playerExecutor { player, args ->
            val einstellung: Setting by args
            val zustand: Boolean? by args

            if (!einstellung.isBoolean()) {
                player.sendMessage(SettingsCommandMessages.booleanOnly)
                return@playerExecutor
            }

            val value = zustand?.let { SettingsCommandActions.set(player.uniqueId, player, einstellung, it) }
                ?: SettingsCommandActions.toggle(player.uniqueId, player, einstellung)
            player.sendMessage(SettingsCommandMessages.changed(einstellung.name, value))
        }
    }

    subcommand("info") {
        withPermission(SettingsPermissions.COMMAND_INFO)
        settingArgument("einstellung")

        playerExecutor { player, args ->
            val einstellung: Setting by args
            player.sendMessage(SettingsCommandMessages.info(einstellung.name, SettingsCommandActions.get(player.uniqueId, einstellung)))
        }
    }
}

/**
 * The actions of `/surfplayer settings`.
 */
fun settingsAdminActions() = listOf(
    PaperAdminAction("refresh", SettingsPermissions.ADMIN_REFRESH, "Lädt alle Einstellungen neu") {
        anyExecutorSuspend { sender, _ ->
            val millis = measureTimeMillis { SettingsService.refreshSettings() }
            sender.sendMessage(SettingsCommandMessages.refreshed(SettingsService.settings.size, millis))
        }
    },
    PaperAdminAction("list", SettingsPermissions.ADMIN_LIST, "Zeigt alle verfügbaren Einstellungen") {
        anyExecutor { sender, _ ->
            if (SettingsService.settings.isEmpty()) {
                sender.sendMessage(SettingsCommandMessages.noSettings)
            } else {
                sender.sendMessage(SettingsCommandMessages.list(SettingsService.settingsByDeclaration()))
            }
        }
    },
)
