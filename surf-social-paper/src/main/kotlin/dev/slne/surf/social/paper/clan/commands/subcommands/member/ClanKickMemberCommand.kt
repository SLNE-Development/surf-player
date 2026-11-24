package dev.slne.surf.social.paper.clan.commands.subcommands.member

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

fun CommandAPICommand.clanKickMemberCommand() = subcommand("kick") {
    withPermission(ClanPermissions.CLAN_KICK_MEMBER_COMMAND)

    argument(ClanMemberArgument("member"))

    playerExecutorSuspend { player, args ->
        val member = args.awaiting<ClanMember>("member")
        val clan = Clan.byPlayer(player.uniqueId)
            ?: failCommand(Messages.NOT_IN_CLAN)

        if (player.uniqueId == member.uuid) {
            failCommand(CANNOT_KICK_SELF)
        }

        if (!clan.hasMemberPermission(player.uniqueId, ClanPermission.KICK)) {
            failCommand(NO_KICK_PERMISSION)
        }

        val executorMember = clan.getMember(player.uniqueId)
            ?: failCommand(Messages.NOT_IN_CLAN)

        if (member.role >= executorMember.role) {
            failCommand(CANNOT_KICK_SAME_OR_HIGHER_ROLE)
        }

        val removed = clan.removeMember(member)

        if (!removed) {
            failCommand(KICK_FAILED)
        }

        val memberName = PlayerLookupService.getUsername(member.uuid) ?: member.uuid.toString()

        clan.broadcast(memberKickedMessage(memberName, player.name))
    }
}
