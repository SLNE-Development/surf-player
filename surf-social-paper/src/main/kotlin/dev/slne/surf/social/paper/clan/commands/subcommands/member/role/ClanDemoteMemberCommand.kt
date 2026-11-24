package dev.slne.surf.social.paper.clan.commands.subcommands.member.role

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.api.clan.member.ClanMember
import dev.slne.surf.social.core.client.clan.command.RoleChange
import dev.slne.surf.social.paper.clan.commands.arguments.ClanMemberArgument
import dev.slne.surf.social.paper.clan.permission.ClanPermissions

fun CommandAPICommand.clanDemoteMemberCommand() = subcommand("demote") {
    withPermission(ClanPermissions.CLAN_DEMOTE_MEMBER_COMMAND)

    argument(ClanMemberArgument("member"))

    playerExecutorSuspend { player, args ->
        changeMemberRole(player, args.awaiting<ClanMember>("member"), RoleChange.DEMOTED)
    }
}
