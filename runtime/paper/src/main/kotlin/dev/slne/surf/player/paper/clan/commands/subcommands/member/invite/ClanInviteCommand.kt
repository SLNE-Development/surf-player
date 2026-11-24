package dev.slne.surf.player.paper.clan.commands.subcommands.member.invite

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.permission.ClanPermission
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.*
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer

fun CommandAPICommand.clanInviteCommand() = subcommand("invite") {
    withPermission(ClanPermissions.CLAN_INVITE_COMMAND)

    surfOfflinePlayerArgument("invitee")

    playerExecutorSuspend { player, args ->
        val invitee = args.awaitingOrNull<SurfPlayer?>("invitee")
            ?: failCommand(Messages.PLAYER_NOT_FOUND)
        val clan = SurfClanApi.findClanByPlayer(player.uniqueId)
            ?: failCommand(Messages.NOT_IN_CLAN)

        if (!clan.hasMemberPermission(player.uniqueId, ClanPermission.INVITE)) {
            failCommand(NO_INVITE_PERMISSION)
        }

        when (clan.invite(invitee.uuid, player.uniqueId)) {
            ClanInviteResult.AlreadyInClan -> failCommand(INVITEE_ALREADY_IN_CLAN)
            ClanInviteResult.AlreadyInvited -> failCommand(INVITEE_ALREADY_INVITED)
            ClanInviteResult.InvitationsDisabled -> failCommand(
                INVITEE_DISABLED_INVITATIONS
            )

            is ClanInviteResult.Success -> {
                val inviteeName = invitee.lastKnownName ?: invitee.uuid.toString()

                player.sendMessage(inviteSentMessage(inviteeName, clan as ClanImpl))

                SurfCoreApi.getPlayer(invitee.uuid)?.let { inviteeTarget ->
                    SurfCoreApi.sendText(
                        inviteeTarget,
                        inviteReceivedMessage(player.name, clan.name)
                    )
                }
            }
        }
    }
}
