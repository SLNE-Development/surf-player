package dev.slne.surf.player.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.clan.commands.playerClickEvent
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.CLAN_OWNER_CANNOT_LEAVE
import dev.slne.surf.player.core.client.clan.command.handleLeaveClick
import dev.slne.surf.player.core.client.clan.command.leaveConfirmationMessage
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

fun CommandAPICommand.clanLeaveCommand() = subcommand("leave") {
    withPermission(ClanPermissions.CLAN_LEAVE_COMMAND)

    playerExecutorSuspend { player, args ->
        val clan = Clan.byPlayer(player.uniqueId)
            ?: failCommand(Messages.NOT_IN_CLAN)

        if (clan.createdByUuid == player.uniqueId) {
            failCommand(CLAN_OWNER_CANNOT_LEAVE)
        }

        player.sendMessage(leaveConfirmationMessage(clan, createConfirmCallback(clan.uuid)))
    }
}

private fun createConfirmCallback(originalClanUuid: UUID) =
    playerClickEvent(1.minutes.toJavaDuration()) { clicked ->
        handleLeaveClick(clicked, clicked.uniqueId, clicked.name, originalClanUuid)
    }
