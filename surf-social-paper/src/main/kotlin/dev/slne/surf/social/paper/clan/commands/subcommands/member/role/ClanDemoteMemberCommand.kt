package dev.slne.surf.social.paper.clan.commands.subcommands.member.role

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.member.ClanMember
import dev.slne.surf.social.api.clan.permission.ClanPermission
import dev.slne.surf.social.paper.clan.commands.arguments.ClanMemberArgument
import dev.slne.surf.social.paper.clan.commands.failCommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.service.PlayerLookupService
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.command.*

fun CommandAPICommand.clanDemoteMemberCommand() = subcommand("demote") {
    withPermission(ClanPermissions.CLAN_DEMOTE_MEMBER_COMMAND)

    argument(ClanMemberArgument("member"))

    playerExecutorSuspend { player, args ->
        val member = args.awaiting<ClanMember>("member")
        val clan = Clan.byPlayer(player.uniqueId)
            ?: failCommand(Messages.NOT_IN_CLAN)

        if (!clan.isMember(member.uuid)) {
            failCommand(Messages.PLAYER_NOT_IN_YOUR_CLAN)
        }

        if (member.uuid == player.uniqueId) {
            failCommand(CANNOT_DEMOTE_SELF)
        }

        if (!clan.hasMemberPermission(player.uniqueId, ClanPermission.DEMOTE)) {
            failCommand(NO_DEMOTE_PERMISSION)
        }

        val executorMember = clan.getMember(player.uniqueId)
            ?: failCommand(Messages.NOT_IN_CLAN)

        if (member.role >= executorMember.role) {
            failCommand(CANNOT_DEMOTE_SAME_OR_HIGHER_ROLE)
        }

        if (!member.role.hasPreviousRole()) {
            failCommand(ALREADY_LOWEST_ROLE)
        }

        val oldRole = member.role
        val newRole = member.role.previousRole()
        val changedRole = member.changeRole(newRole)

        if (!changedRole) {
            failCommand(ROLE_CHANGE_FAILED)
        }

        val memberName = PlayerLookupService.getUsername(member.uuid) ?: member.uuid.toString()

        clan.broadcast(
            memberRoleChangedMessage(
                memberName,
                player.name,
                oldRole,
                newRole,
                RoleChange.DEMOTED
            )
        )
    }
}
