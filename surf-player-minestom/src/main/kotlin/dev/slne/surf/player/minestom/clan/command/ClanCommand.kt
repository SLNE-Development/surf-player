package dev.slne.surf.social.minestom.clan.command

import dev.slne.surf.social.minestom.clan.command.subcommands.*
import dev.slne.surf.social.minestom.clan.command.subcommands.admin.clanAdminCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.member.clanKickMemberCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.member.clanMembersCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.member.invite.clanAcceptCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.member.invite.clanDenyCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.member.invite.clanInviteCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.member.role.clanDemoteMemberCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.member.role.clanPromoteMemberCommand
import dev.slne.surf.social.minestom.clan.command.subcommands.player.clanPlayerCommand
import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions

fun clanCommand() = commandAPICommand("clan") {
    withPermission(ClanPermissions.CLAN_COMMAND)

    clanCreateCommand()
    clanDisbandCommand()
    clanLeaveCommand()
    clanInfoCommand()
    clanWhoisCommand()
    clanSetDiscordCommand()

    clanInviteCommand()
    clanAcceptCommand()
    clanDenyCommand()

    clanPromoteMemberCommand()
    clanDemoteMemberCommand()
    clanKickMemberCommand()
    clanMembersCommand()

    clanPlayerCommand()
    clanAdminCommand()

    clanOptionsCommand()
    clanListCommand()

    withSubcommand(clanChatCommand("chat"))
}
