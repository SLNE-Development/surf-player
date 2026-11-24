package dev.slne.surf.player.minestom.chat.command.surfchat

import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.minestom.chat.command.surfchat.functionality.functionalityCommand

fun surfChatCommand() = commandAPICommand("surfchat") {
    withPermission(ChatPermissions.COMMAND_SURFCHAT)
    withAliases("sc")

    surfChatLookupCommand()
    surfChatLookupHelpCommand()
    surfChatReloadCommand()
    functionalityCommand()
}
