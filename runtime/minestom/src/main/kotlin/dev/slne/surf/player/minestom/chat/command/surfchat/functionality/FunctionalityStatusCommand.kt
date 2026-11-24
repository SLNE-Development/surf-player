package dev.slne.surf.player.minestom.chat.command.surfchat.functionality

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.service.FunctionalityService

fun CommandAPICommand.functionalityStatusCommand(): CommandAPICommand = withSubcommand(
    subcommand("status") {
        withPermission(ChatPermissions.COMMAND_SURFCHAT_FUNCTIONALITY_STATUS)
        anyExecutor { player, _ ->
            player.sendText {
                appendInfoPrefix()
                info("Der Chat ist derzeit ")
                variableValue(if (FunctionalityService.getFunctionalities().localChatEnabled) "aktiviert" else "deaktiviert")
                info(".")
            }
        }
    }
)
