package dev.slne.surf.player.paper.chat.command.surfchat

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.player.paper.chat.command.surfchat.functionality.functionalityCommand
import dev.slne.surf.player.paper.chat.permission.PermissionRegistry

fun surfChatCommand() = commandAPICommand("surfchat") {
    withPermission(PermissionRegistry.COMMAND_SURFCHAT)
    withAliases("sc")

    surfChatLookupCommand()
    surfChatLookupHelpCommand()
    surfChatReloadCommand()
    functionalityCommand()
}