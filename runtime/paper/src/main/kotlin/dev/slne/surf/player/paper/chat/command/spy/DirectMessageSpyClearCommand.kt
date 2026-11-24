package dev.slne.surf.player.paper.chat.command.spy

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.core.common.chat.service.SpyService
import dev.slne.surf.player.paper.chat.permission.PermissionRegistry

fun CommandAPICommand.directMessageSpyClearCommand() = subcommand("#clear") {
    withPermission(PermissionRegistry.COMMAND_DIRECT_SPY_CLEAR)
    playerExecutor { player, _ ->
        if (!SpyService.isPrivateMessageSpying(player.uniqueId)) {
            player.sendText {
                appendErrorPrefix()
                error("Du spionierst aktuell bei keinem Spieler.")
            }
            return@playerExecutor
        }

        SpyService.clearPrivateMessageSpies(player.uniqueId)

        player.sendText {
            appendSuccessPrefix()
            success("Du spionierst jetzt bei keinem Spieler mehr.")
        }
    }
}