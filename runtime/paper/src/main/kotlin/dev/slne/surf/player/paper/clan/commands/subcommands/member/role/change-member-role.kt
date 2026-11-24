package dev.slne.surf.player.paper.clan.commands.subcommands.member.role

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.permission.ClanPermission
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.*
import dev.slne.surf.player.core.client.util.usernameOrUuid
import dev.slne.surf.player.paper.clan.commands.failCommand
import org.bukkit.entity.Player

/**
 * Promotes or demotes [member] by one role on behalf of [player] and announces it to the clan.
 */
suspend fun changeMemberRole(player: Player, member: ClanMember, change: RoleChange) {
    val clan = SurfClanApi.findClanByPlayer(player.uniqueId)
        ?: failCommand(Messages.NOT_IN_CLAN)

    if (!clan.isMember(member.uuid)) {
        failCommand(Messages.PLAYER_NOT_IN_YOUR_CLAN)
    }

    if (member.uuid == player.uniqueId) {
        failCommand(change.pick(CANNOT_PROMOTE_SELF, CANNOT_DEMOTE_SELF))
    }

    if (!clan.hasMemberPermission(player.uniqueId, change.pick(ClanPermission.PROMOTE, ClanPermission.DEMOTE))) {
        failCommand(change.pick(NO_PROMOTE_PERMISSION, NO_DEMOTE_PERMISSION))
    }

    val executorMember = clan.getMember(player.uniqueId)
        ?: failCommand(Messages.NOT_IN_CLAN)

    if (member.role >= executorMember.role) {
        failCommand(change.pick(CANNOT_PROMOTE_SAME_OR_HIGHER_ROLE, CANNOT_DEMOTE_SAME_OR_HIGHER_ROLE))
    }

    val oldRole = member.role
    val canChange = change.pick(oldRole.hasNextRole(), oldRole.hasPreviousRole())
    if (!canChange) {
        failCommand(change.pick(ALREADY_HIGHEST_ROLE, ALREADY_LOWEST_ROLE))
    }

    val newRole = when (change) {
        RoleChange.PROMOTED -> oldRole.nextRole()
        RoleChange.DEMOTED -> oldRole.previousRole()
    }
    if (!member.changeRole(newRole)) {
        failCommand(ROLE_CHANGE_FAILED)
    }

    clan.broadcast(memberRoleChangedMessage(usernameOrUuid(member.uuid), player.name, oldRole, newRole, change))
}

private fun <T> RoleChange.pick(promoted: T, demoted: T): T = when (this) {
    RoleChange.PROMOTED -> promoted
    RoleChange.DEMOTED -> demoted
}
