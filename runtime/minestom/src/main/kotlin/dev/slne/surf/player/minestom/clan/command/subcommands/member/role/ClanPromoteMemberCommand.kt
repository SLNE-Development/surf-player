package dev.slne.surf.player.minestom.clan.command.subcommands.member.role

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.permission.ClanPermission
import dev.slne.surf.player.minestom.clan.command.arguments.clanMemberArgument
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.player.minestom.clan.command.resolveClanMember
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.core.client.util.usernameOrUuid
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.*
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions

fun CommandAPICommand.clanPromoteMemberCommand(): CommandAPICommand = withSubcommand(
    subcommand("promote") {
        withPermission(ClanPermissions.CLAN_PROMOTE_MEMBER_COMMAND)

        clanMemberArgument("member")

        playerExecutorSuspend { player, args ->
            val member = args.resolveClanMember("member")
            val clan = SurfClanApi.findClanByPlayer(player.uuid)
                ?: failCommand(Messages.NOT_IN_CLAN)
            val isInClan = clan.isMember(member.uuid)

            if (!isInClan) {
                failCommand(Messages.PLAYER_NOT_IN_YOUR_CLAN)
            }

            if (member.uuid == player.uuid) {
                failCommand(CANNOT_PROMOTE_SELF)
            }

            if (!clan.hasMemberPermission(player.uuid, ClanPermission.PROMOTE)) {
                failCommand(NO_PROMOTE_PERMISSION)
            }

            val executorMember = clan.getMember(player.uuid)
                ?: failCommand(Messages.NOT_IN_CLAN)

            if (executorMember.role <= member.role) {
                failCommand(CANNOT_PROMOTE_SAME_OR_HIGHER_ROLE)
            }

            if (!member.role.hasNextRole()) {
                failCommand(ALREADY_HIGHEST_ROLE)
            }

            val oldRole = member.role
            val newRole = member.role.nextRole()
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
                    RoleChange.PROMOTED
                )
            )
        }
    }
)
