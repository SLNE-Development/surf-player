package dev.slne.surf.player.paper.settings.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.player.core.client.settings.command.SettingsCommandMessages
import dev.slne.surf.player.core.common.settings.service.SettingsService
import dev.slne.surf.player.paper.settings.permission.PermissionRegistry
import dev.slne.surf.player.paper.settings.plugin
import kotlin.system.measureTimeMillis

fun surfSettingsCommand() = commandTree("surfSettings") {
    withPermission(PermissionRegistry.COMMAND_SURF_SETTINGS)
    literalArgument("refresh") {
        withPermission(PermissionRegistry.COMMAND_SURF_SETTINGS_REFRESH)
        anyExecutor { executor, _ ->
            plugin.launch {
                val ms = measureTimeMillis {
                    SettingsService.refreshSettings()
                }

                executor.sendMessage(
                    SettingsCommandMessages.refreshed(SettingsService.settings.size, ms)
                )
            }
        }
    }

    literalArgument("list") {
        withPermission(PermissionRegistry.COMMAND_SURF_SETTINGS_LIST)
        anyExecutor { executor, _ ->
            if (SettingsService.settings.isEmpty()) {
                executor.sendMessage(SettingsCommandMessages.noSettings)
                return@anyExecutor
            }

            executor.sendMessage(SettingsCommandMessages.list(SettingsService.settings))
        }
    }
}
