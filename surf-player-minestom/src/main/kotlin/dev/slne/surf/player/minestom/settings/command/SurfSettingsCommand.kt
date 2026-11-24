package dev.slne.surf.social.minestom.settings.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.social.core.client.settings.command.SettingsCommandMessages
import dev.slne.surf.social.core.client.settings.permission.SettingsPermissions
import dev.slne.surf.social.core.common.settings.service.SettingsService
import kotlin.system.measureTimeMillis

fun surfSettingsCommand() = commandTree("surfSettings") {
    withPermission(SettingsPermissions.COMMAND_SURF_SETTINGS)

    literalArgument("refresh") {
        withPermission(SettingsPermissions.COMMAND_SURF_SETTINGS_REFRESH)
        anyExecutorSuspend { executor, _ ->
            val elapsedMillis = measureTimeMillis {
                SettingsService.refreshSettings()
            }

            executor.sendMessage(
                SettingsCommandMessages.refreshed(
                    SettingsService.settings.size,
                    elapsedMillis
                )
            )
        }
    }

    literalArgument("list") {
        withPermission(SettingsPermissions.COMMAND_SURF_SETTINGS_LIST)
        anyExecutor { executor, _ ->
            if (SettingsService.settings.isEmpty()) {
                executor.sendMessage(SettingsCommandMessages.noSettings)
                return@anyExecutor
            }

            executor.sendMessage(SettingsCommandMessages.list(SettingsService.settings))
        }
    }
}
