package dev.slne.surf.social.minestom.clan.command.subcommands.member.invite

import dev.slne.surf.social.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.social.minestom.clan.command.arguments.clanInviteArgument
import dev.slne.surf.social.minestom.command.failCommand
import dev.slne.surf.social.minestom.clan.command.resolveClanInvite
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.command.inviteAcceptedMessage
import dev.slne.surf.social.core.client.clan.command.memberJoinedMessage
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions

fun CommandAPICommand.clanAcceptCommand(): CommandAPICommand = withSubcommand(
    subcommand("accept") {
        withPermission(ClanPermissions.CLAN_ACCEPT_INVITE_COMMAND)

        clanInviteArgument("invite")

        playerExecutorSuspend { player, args ->
            val invite = args.resolveClanInvite("invite")

            when (val result = invite.accept()) {
                ClanInviteAcceptResult.AlreadyInClan -> failCommand(Messages.ALREADY_IN_CLAN)
                is ClanInviteAcceptResult.Accepted -> {
                    val clan = result.clan as ClanImpl

                    player.sendMessage(inviteAcceptedMessage(clan))
                    clan.broadcast(memberJoinedMessage(player.username))
                }
            }
        }
    }
)
