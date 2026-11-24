package dev.slne.surf.social.minestom.chat.command.direct

import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.signedMessageArgument
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.minestom.chat.service.DirectMessageService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.surfPlayerArgument
import net.kyori.adventure.chat.SignedMessage

fun directMessageCommand() = commandAPICommand("msg") {
    withPermission(ChatPermissions.COMMAND_PM)
    withAliases("dm", "w", "whisper", "tell", "pm")
    surfPlayerArgument("target")
    signedMessageArgument("message")

    playerExecutorSuspend { player, args ->
        val target: SurfPlayer by args
        val message: SignedMessage by args

        if (target.uuid == player.uuid) {
            player.sendText {
                appendErrorPrefix()
                error("Du kannst dir selbst keine privaten Nachrichten senden!")
            }
            return@playerExecutorSuspend
        }

        DirectMessageService.sendMessage(player, message, target.uuid)
    }
}
