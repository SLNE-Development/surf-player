package dev.slne.surf.social.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.optionalArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.paper.clan.commands.arguments.ClanByClanTagArgument
import dev.slne.surf.social.paper.clan.commands.failCommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.components.Components


fun CommandAPICommand.clanInfoCommand() = subcommand("info") {
    withPermission(ClanPermissions.CLAN_INFO_COMMAND)

    optionalArgument(ClanByClanTagArgument("clanTag"))

    playerExecutorSuspend { player, args ->
        val clan = args.awaitingOrNull<Clan>("clanTag") ?: Clan.byPlayer(player.uniqueId)

        if (clan == null) {
            failCommand(Messages.NOT_IN_CLAN)
        }

        player.sendMessage(Components.Clan.renderClanInformation(clan as ClanImpl))
    }
}
