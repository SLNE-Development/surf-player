package dev.slne.surf.social.minestom.chat.command.spy

import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.api.minestom.command.dsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.common.chat.service.SpyService

fun CommandTree.directMessageSpyClearCommand(): CommandTree = literalArgument("#clear") {
    withPermission(ChatPermissions.COMMAND_DIRECT_SPY_CLEAR)
    playerExecutor { player, _ ->
        if (!SpyService.isPrivateMessageSpying(player.uuid)) {
            player.sendText {
                appendErrorPrefix()
                error("Du spionierst aktuell bei keinem Spieler.")
            }
            return@playerExecutor
        }

        SpyService.clearPrivateMessageSpies(player.uuid)

        player.sendText {
            appendSuccessPrefix()
            success("Du spionierst jetzt bei keinem Spieler mehr.")
        }
    }
}
