package dev.slne.surf.social.paper.chat.command.surfchat.functionality

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.social.core.common.chat.service.FunctionalityService
import dev.slne.surf.social.paper.chat.command.argument.niceToggleArgument
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry
import dev.slne.surf.core.api.common.SurfCoreApi
import org.bukkit.Bukkit

fun CommandAPICommand.functionalityChangeCommand() = subcommand("change") {
    withPermission(PermissionRegistry.COMMAND_SURFCHAT_FUNCTIONALITY_TOGGLE)
    niceToggleArgument("toggle")
    anyExecutorSuspend { player, args ->
        val toggle: Boolean by args

        if (toggle) {
            FunctionalityService.updateLocalFunctionalities {
                it.copy(localChatEnabled = true)
            }

            player.sendText {
                appendSuccessPrefix()
                success("Der Chat wurde aktiviert.")
            }

            val message = buildText {
                appendInfoPrefix()
                variableValue(player.name)
                info(" hat den Chat für den Server ")
                variableValue(SurfCoreApi.getCurrentServerName())
                info(" aktiviert.")
            }

            Bukkit.broadcast(message, PermissionRegistry.TEAM_NOTIFY_FUNCTIONALITY)
        } else {
            FunctionalityService.updateLocalFunctionalities {
                it.copy(localChatEnabled = false)
            }
            player.sendText {
                appendSuccessPrefix()
                success("Der Chat wurde deaktiviert.")
            }

            val message = buildText {
                appendSuccessPrefix()
                variableValue(player.name)
                info(" hat den Chat für den Server ")
                variableValue(SurfCoreApi.getCurrentServerName())
                info(" deaktiviert.")
            }

            Bukkit.broadcast(message, PermissionRegistry.TEAM_NOTIFY_FUNCTIONALITY)
        }
    }
}