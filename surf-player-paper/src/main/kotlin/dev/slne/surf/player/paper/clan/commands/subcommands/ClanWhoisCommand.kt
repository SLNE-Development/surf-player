package dev.slne.surf.player.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.whoisMessage
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument

fun CommandAPICommand.clanWhoisCommand() = subcommand("whois") {
    withPermission(ClanPermissions.CLAN_WHOIS_COMMAND)
    surfOfflinePlayerArgument("target")
    playerExecutorSuspend { player, args ->
        val target = args.awaitingOrNull<SurfPlayer?>("target")
            ?: failCommand(Messages.PLAYER_NOT_FOUND)

        val targetName = target.lastKnownName ?: target.uuid.toString()
        player.sendMessage(whoisMessage(targetName, Clan.byPlayer(target.uuid)))
    }
}
