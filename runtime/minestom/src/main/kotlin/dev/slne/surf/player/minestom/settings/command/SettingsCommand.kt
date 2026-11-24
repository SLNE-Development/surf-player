package dev.slne.surf.player.minestom.settings.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutor
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.client.settings.command.SettingsCommandActions
import dev.slne.surf.player.core.client.settings.command.SettingsCommandMessages
import dev.slne.surf.player.core.client.settings.command.settingsCommandHelp
import dev.slne.surf.player.core.client.settings.permission.SettingsPermissions
import dev.slne.surf.player.core.client.settings.service.SettingsService
import dev.slne.surf.player.minestom.command.MinestomAdminAction
import dev.slne.surf.player.minestom.command.helpExecutor
import dev.slne.surf.player.minestom.command.toggleArgument
import dev.slne.surf.player.minestom.settings.command.argument.SettingArgument
import dev.slne.surf.player.minestom.settings.menu.openSettingsMenu
import kotlin.system.measureTimeMillis

private const val SETTING = "einstellung"

/**
 * `/settings` opens the menu; `/settings help|toggle|info` work without it.
 */
fun settingsCommand() = commandAPICommand("settings") {
    withPermission(SettingsPermissions.COMMAND_SETTINGS)

    playerExecutor { player, _ -> player.openSettingsMenu() }

    withSubcommand(subcommand("help") { helpExecutor(settingsCommandHelp) })

    withSubcommand(subcommand("toggle") {
        withPermission(SettingsPermissions.COMMAND_TOGGLE)
        withArguments(SettingArgument(SETTING))
        toggleArgument("zustand", optional = true)

        playerExecutor { player, args ->
            val setting: Setting = args.get(SETTING)
            val state = args.getOptional<Boolean>("zustand")

            if (!setting.isBoolean()) {
                player.sendMessage(SettingsCommandMessages.booleanOnly)
                return@playerExecutor
            }

            val value = state?.let { SettingsCommandActions.set(player.uuid, setting, it) }
                ?: SettingsCommandActions.toggle(player.uuid, setting)
            player.sendMessage(SettingsCommandMessages.changed(setting.name, value))
        }
    })

    withSubcommand(subcommand("info") {
        withPermission(SettingsPermissions.COMMAND_INFO)
        withArguments(SettingArgument(SETTING))

        playerExecutor { player, args ->
            val setting: Setting = args.get(SETTING)
            player.sendMessage(SettingsCommandMessages.info(setting.name, SettingsCommandActions.get(player.uuid, setting)))
        }
    })
}

/**
 * The actions of `/surfplayer settings`.
 */
fun settingsAdminActions() = listOf(
    MinestomAdminAction("refresh", SettingsPermissions.ADMIN_REFRESH, "Lädt alle Einstellungen neu") {
        anyExecutorSuspend { sender, _ ->
            val millis = measureTimeMillis { SettingsService.refreshSettings() }
            sender.sendMessage(SettingsCommandMessages.refreshed(SettingsService.settings.size, millis))
        }
    },
    MinestomAdminAction("list", SettingsPermissions.ADMIN_LIST, "Zeigt alle verfügbaren Einstellungen") {
        anyExecutor { sender, _ ->
            if (SettingsService.settings.isEmpty()) {
                sender.sendMessage(SettingsCommandMessages.noSettings)
            } else {
                sender.sendMessage(SettingsCommandMessages.list(SettingsService.settings))
            }
        }
    },
)
