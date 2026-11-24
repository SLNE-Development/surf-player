package dev.slne.surf.social.minestom.chat.command.surfchat.functionality

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.client.chat.platform.ChatPlatform
import dev.slne.surf.social.core.common.chat.service.FunctionalityService
import dev.slne.surf.social.minestom.chat.command.argument.niceToggleArgument
import dev.slne.surf.core.api.common.SurfCoreApi
import net.minestom.server.entity.Player

fun CommandAPICommand.functionalityChangeCommand(): CommandAPICommand = withSubcommand(
    subcommand("change") {
        withPermission(ChatPermissions.COMMAND_SURFCHAT_FUNCTIONALITY_TOGGLE)
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

            val notification = buildText {
                appendInfoPrefix()
                variableValue(if (sender is Player) sender.username else "Console")
                info(" hat den Chat für den Server ")
                variableValue(SurfCoreApi.getCurrentServerName())
                info(" $state.")
            }

            ChatPlatform.broadcast(notification, ChatPermissions.TEAM_NOTIFY_FUNCTIONALITY)
        }
    }
)
