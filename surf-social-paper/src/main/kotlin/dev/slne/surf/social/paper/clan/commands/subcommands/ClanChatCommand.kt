package dev.slne.surf.social.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.args.asyncSignedMessageArgument
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.api.chat.SurfChatApi
import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.api.chat.message.MessageType
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.chat.formatClanChatMessage
import dev.slne.surf.social.core.client.clan.chat.prefixedName
import dev.slne.surf.core.api.common.server.SurfServer
import kotlinx.coroutines.launch
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import java.time.OffsetDateTime
import java.util.*

class ClanChatCommand(name: String) : CommandAPICommand(name) {
    init {
        withPermission(ClanPermissions.CLAN_CHAT_COMMAND)

        asyncSignedMessageArgument("message")

        playerExecutorSuspend { player, arguments ->
            val playerClan = Clan.byPlayer(player.uniqueId)
            val message = arguments.awaiting<SignedMessage>("message")

            if (playerClan == null) {
                player.sendText {
                    appendErrorPrefix()
                    error(Messages.NOT_IN_CLAN)
                }
                return@playerExecutorSuspend
            }

            val formattedMessage =
                formatClanChatMessage(message.message(), prefixedName(player.uniqueId, player.name))

            playerClan.members.forEach { member ->
                launch {
                    SurfChatApi.sendSignedMessage(
                        message,
                        player.uniqueId,
                        member.uuid,
                        formattedMessage
                    )
                }
            }

            val messageData = MessageData(
                message = Component.text(message.message()),
                messageUuid = UUID.randomUUID(),
                sender = player.uniqueId,
                receiver = null,
                sentAt = OffsetDateTime.now(),
                server = SurfServer.current().name,
                signature = message.signature(),
                type = MessageType("CLAN_CHAT")
            )

            SurfChatApi.logMessage(messageData)
            SurfChatApi.passAutoMod(messageData)
        }
    }
}
