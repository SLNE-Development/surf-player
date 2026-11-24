package dev.slne.surf.social.paper.chat.command.direct

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.args.asyncSignedMessageArgument
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry
import dev.slne.surf.social.paper.chat.service.DirectMessageSender
import net.kyori.adventure.chat.SignedMessage

fun directMessageCommand() = commandAPICommand("msg") {
    withPermission(PermissionRegistry.COMMAND_PM)
    withAliases("dm", "w", "whisper", "tell", "pm")
    surfPlayerArgument("target")
    asyncSignedMessageArgument("message")

    playerExecutorSuspend { player, args ->
        val target: SurfPlayer by args
        val message = args.awaiting<SignedMessage>("message")

        if (target.uuid == player.uniqueId) {
            player.sendText {
                appendErrorPrefix()
                error("Du kannst dir selbst keine privaten Nachrichten senden!")
            }
            return@playerExecutorSuspend
        }

        DirectMessageSender.sendMessage(player, message, target.uuid)
    }
}
