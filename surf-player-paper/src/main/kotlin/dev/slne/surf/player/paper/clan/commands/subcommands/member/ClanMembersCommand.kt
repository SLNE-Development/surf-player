package dev.slne.surf.social.paper.clan.commands.subcommands.member

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
import dev.slne.surf.social.core.client.clan.command.clanMembersPagination
import dev.slne.surf.social.core.client.clan.command.collectClanMemberData

fun CommandAPICommand.clanMembersCommand() = subcommand("members") {
    withPermission(ClanPermissions.CLAN_VIEW_MEMBERS_COMMAND)

    optionalArgument(ClanByClanTagArgument("clan"))

    playerExecutorSuspend { player, args ->
        val clan = args.awaitingOrNull<Clan>("clan") ?: run {
            Clan.byPlayer(player.uniqueId)
                ?: failCommand(Messages.NOT_IN_CLAN)
        }

        val data = collectClanMemberData(clan)
        val pagination = clanMembersPagination(clan as ClanImpl)

        player.sendMessage(pagination.renderComponent(data))
    }
}
