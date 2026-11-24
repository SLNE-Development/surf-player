package dev.slne.surf.player.minestom.clan.command.subcommands.member.invite

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.permission.ClanPermission
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.*
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import kotlinx.coroutines.Deferred

fun CommandAPICommand.clanInviteCommand(): CommandAPICommand = withSubcommand(
    subcommand("invite") {
        withPermission(ClanPermissions.CLAN_INVITE_COMMAND)

        surfOfflinePlayerArgument("invitee")

        playerExecutorSuspend { player, args ->
            val invitee = args.get<Deferred<SurfPlayer?>>("invitee").await()
                ?: failCommand(Messages.PLAYER_NOT_FOUND)
            val clan = Clan.byPlayer(player.uuid)
                ?: failCommand(Messages.NOT_IN_CLAN)

            if (!clan.hasMemberPermission(player.uuid, ClanPermission.INVITE)) {
                failCommand(NO_INVITE_PERMISSION)
            }

            when (clan.invite(invitee.uuid, player.uuid)) {
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
                            inviteReceivedMessage(player.username, clan.name)
                        )
                    }
                }
            }
        }
    }
)
