package dev.slne.surf.player.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.clan.commands.playerClickEvent
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.canBeDisbandedBy
import dev.slne.surf.player.core.client.clan.command.disbandConfirmationMessage
import dev.slne.surf.player.core.client.clan.command.handleDisbandClick
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

fun CommandAPICommand.clanDisbandCommand() = subcommand("disband") {
    withPermission(ClanPermissions.CLAN_DISBAND_COMMAND)

    playerExecutorSuspend { player, args ->
        val playerUuid = player.uniqueId
        val clan =
            Clan.byPlayer(playerUuid) ?: failCommand(Messages.NOT_IN_CLAN)

        clan.canBeDisbandedBy(playerUuid)?.let { error ->
            failCommand(error.message)
        }

        player.sendMessage(disbandConfirmationMessage(clan, createDisbandClickEvent(clan.uuid)))
    }
}

private fun createDisbandClickEvent(oldClanUuid: UUID) =
    playerClickEvent(1.minutes.toJavaDuration()) { clicked ->
        handleDisbandClick(clicked, clicked.uniqueId, oldClanUuid)
    }
