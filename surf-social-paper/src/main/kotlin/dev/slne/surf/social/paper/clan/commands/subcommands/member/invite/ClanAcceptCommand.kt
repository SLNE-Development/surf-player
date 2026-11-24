package dev.slne.surf.social.paper.clan.commands.subcommands.member.invite

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.api.clan.invite.ClanInvite
import dev.slne.surf.social.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.social.paper.clan.commands.arguments.ClanInviteArgument
import dev.slne.surf.social.paper.clan.commands.failCommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.command.inviteAcceptedMessage
import dev.slne.surf.social.core.client.clan.command.memberJoinedMessage

fun CommandAPICommand.clanAcceptCommand() = subcommand("accept") {
    withPermission(ClanPermissions.CLAN_ACCEPT_INVITE_COMMAND)

    argument(ClanInviteArgument("invite"))
    playerExecutorSuspend { player, args ->
        val invite = args.awaiting<ClanInvite>("invite")

        when (val result = invite.accept()) {
            ClanInviteAcceptResult.AlreadyInClan -> failCommand(Messages.ALREADY_IN_CLAN)
            is ClanInviteAcceptResult.Accepted -> {
                val clan = result.clan as ClanImpl

                player.sendMessage(inviteAcceptedMessage(clan))
                clan.broadcast(memberJoinedMessage(player.name))
            }
        }
    }
}
