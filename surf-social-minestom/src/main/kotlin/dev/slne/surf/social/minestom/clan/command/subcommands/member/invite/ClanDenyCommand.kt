package dev.slne.surf.social.minestom.clan.command.subcommands.member.invite

import dev.slne.surf.social.minestom.clan.command.arguments.clanInviteArgument
import dev.slne.surf.social.minestom.clan.command.failCommand
import dev.slne.surf.social.minestom.clan.command.resolveClanInvite
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.client.clan.command.INVITE_ALREADY_DENIED
import dev.slne.surf.social.core.client.clan.command.inviteDeniedMessage
import dev.slne.surf.social.core.client.clan.command.inviteDeniedNotification
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions
import dev.slne.surf.core.api.common.SurfCoreApi

fun CommandAPICommand.clanDenyCommand(): CommandAPICommand = withSubcommand(
    subcommand("deny") {
        withPermission(ClanPermissions.CLAN_DENY_INVITE_COMMAND)

        clanInviteArgument("invite")

        playerExecutorSuspend { player, args ->
            val invite = args.resolveClanInvite("invite")

            val revoked = invite.revoke()

            if (!revoked) {
                failCommand(INVITE_ALREADY_DENIED)
            } else {
                player.sendMessage(inviteDeniedMessage())

                SurfCoreApi.getPlayer(invite.invitedBy)?.let { inviter ->
                    SurfCoreApi.sendText(inviter, inviteDeniedNotification(player.username))
                }
            }
        }
    }
)
