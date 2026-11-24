package dev.slne.surf.player.paper.chat.command

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.paper.command.args.asyncSignedMessageArgument
import dev.slne.surf.player.paper.command.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.chat.command.ChatCommands
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.service.ReplyCache
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import dev.slne.surf.player.paper.chat.service.DirectMessageSender
import dev.slne.surf.player.paper.command.asViewer
import dev.slne.surf.player.paper.command.failCommand
import dev.slne.surf.player.paper.command.visiblePlayerArgument
import net.kyori.adventure.chat.SignedMessage

/**
 * `/msg <spieler> <nachricht>` with the aliases `/dm`, `/w`, `/whisper`, `/tell` and `/pm`.
 */
fun directMessageCommand() = commandAPICommand("msg") {
    withPermission(ChatPermissions.COMMAND_MSG)
    withAliases("dm", "w", "whisper", "tell", "pm")
    visiblePlayerArgument("spieler")
    asyncSignedMessageArgument("nachricht")

    playerExecutorSuspend { player, args ->
        val spieler: SurfPlayer by args
        val message = args.awaiting<SignedMessage>("nachricht")

        if (spieler.uuid == player.uniqueId) failCommand(ChatCommands.CANNOT_MESSAGE_YOURSELF)

        DirectMessageSender.sendMessage(player, message, spieler.uuid)
    }
}

/**
 * `/reply <nachricht>` (`/r`): answers the last private message.
 */
fun replyCommand() = commandAPICommand("reply") {
    withPermission(ChatPermissions.COMMAND_REPLY)
    withAliases("r")
    asyncSignedMessageArgument("nachricht")

    playerExecutorSuspend { player, args ->
        val message = args.awaiting<SignedMessage>("nachricht")
        val target = ReplyCache.getLastTarget(player.uniqueId) ?: failCommand(ChatCommands.NO_REPLY_TARGET)

        if (target == player.uniqueId) failCommand(ChatCommands.CANNOT_MESSAGE_YOURSELF)
        // A vanished target is treated like an offline one.
        PlayerVisibility.network.findOnlinePlayer(player.asViewer(), target)
            ?: failCommand(CommandMessages.PLAYER_NOT_ONLINE)

        DirectMessageSender.sendMessage(player, message, target)
    }
}
