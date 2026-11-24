package dev.slne.surf.social.minestom.clan.command.subcommands.member.role

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.permission.ClanPermission
import dev.slne.surf.social.minestom.clan.command.arguments.clanMemberArgument
import dev.slne.surf.social.minestom.command.failCommand
import dev.slne.surf.social.minestom.clan.command.resolveClanMember
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.client.util.usernameOrUuid
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.command.*
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions

fun CommandAPICommand.clanDemoteMemberCommand(): CommandAPICommand = withSubcommand(
    subcommand("demote") {
        withPermission(ClanPermissions.CLAN_DEMOTE_MEMBER_COMMAND)

        clanMemberArgument("member")

        playerExecutorSuspend { player, args ->
            val member = args.resolveClanMember("member")
            val clan = Clan.byPlayer(player.uuid)
                ?: failCommand(Messages.NOT_IN_CLAN)

            if (!clan.isMember(member.uuid)) {
                failCommand(Messages.PLAYER_NOT_IN_YOUR_CLAN)
            }

            if (member.uuid == player.uuid) {
                failCommand(CANNOT_DEMOTE_SELF)
            }

            if (!clan.hasMemberPermission(player.uuid, ClanPermission.DEMOTE)) {
                failCommand(NO_DEMOTE_PERMISSION)
            }

            val executorMember = clan.getMember(player.uuid)
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

            val memberName = usernameOrUuid(member.uuid)

            clan.broadcast(
                memberRoleChangedMessage(
                    memberName,
                    player.username,
                    oldRole,
                    newRole,
                    RoleChange.DEMOTED
                )
            )
        }
    }
)
