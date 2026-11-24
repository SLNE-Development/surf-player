package dev.slne.surf.social.paper.clan.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.social.paper.clan.commands.subcommands.*
import dev.slne.surf.social.paper.clan.commands.subcommands.admin.clanAdminCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.member.clanKickMemberCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.member.clanMembersCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.member.invite.clanAcceptCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.member.invite.clanDenyCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.member.invite.clanInviteCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.member.role.clanDemoteMemberCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.member.role.clanPromoteMemberCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.player.clanPlayerCommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
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

    withSubcommand(ClanChatCommand("chat"))
}