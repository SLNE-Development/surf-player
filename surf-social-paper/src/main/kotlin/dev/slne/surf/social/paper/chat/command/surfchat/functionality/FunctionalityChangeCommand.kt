package dev.slne.surf.social.paper.chat.command.surfchat.functionality

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.social.core.common.chat.service.FunctionalityService
import dev.slne.surf.social.paper.chat.command.argument.niceToggleArgument
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry
import org.bukkit.Bukkit

fun CommandAPICommand.functionalityChangeCommand() = subcommand("change") {
    withPermission(PermissionRegistry.COMMAND_SURFCHAT_FUNCTIONALITY_TOGGLE)
    niceToggleArgument("toggle")
    anyExecutorSuspend { sender, args ->
        val toggle: Boolean by args
        val state = if (toggle) "aktiviert" else "deaktiviert"

        FunctionalityService.updateLocalFunctionalities {
            it.copy(localChatEnabled = toggle)
        }

        sender.sendText {
            appendSuccessPrefix()
            success("Der Chat wurde $state.")
        }

        val teamNotification = buildText {
            appendInfoPrefix()
            variableValue(sender.name)
            info(" hat den Chat für den Server ")
            variableValue(SurfCoreApi.getCurrentServerName())
            info(" $state.")
        }

        Bukkit.broadcast(teamNotification, PermissionRegistry.TEAM_NOTIFY_FUNCTIONALITY)
    }
}
