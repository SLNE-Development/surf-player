package dev.slne.surf.social.paper.chat.command.surfchat.functionality

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry

fun CommandAPICommand.functionalityCommand() = subcommand("functionality") {
    withPermission(PermissionRegistry.COMMAND_SURFCHAT_FUNCTIONALITY)

    functionalityStatusCommand()
    functionalityChangeCommand()
    functionalityListCommand()
}