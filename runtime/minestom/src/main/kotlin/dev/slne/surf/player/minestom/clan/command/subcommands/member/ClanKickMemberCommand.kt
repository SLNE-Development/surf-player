package dev.slne.surf.player.minestom.clan.command.subcommands.member

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

fun CommandAPICommand.clanKickMemberCommand(): CommandAPICommand = withSubcommand(
    subcommand("kick") {
        withPermission(ClanPermissions.CLAN_KICK_MEMBER_COMMAND)

        clanMemberArgument("member")

        playerExecutorSuspend { player, args ->
            val member = args.resolveClanMember("member")
            val clan = SurfClanApi.findClanByPlayer(player.uuid)
                ?: failCommand(Messages.NOT_IN_CLAN)

            if (player.uuid == member.uuid) {
                failCommand(CANNOT_KICK_SELF)
            }

            if (!clan.hasMemberPermission(player.uuid, ClanPermission.KICK)) {
                failCommand(NO_KICK_PERMISSION)
            }

            val executorMember = clan.getMember(player.uuid)
                ?: failCommand(Messages.NOT_IN_CLAN)

            if (member.role >= executorMember.role) {
                failCommand(CANNOT_KICK_SAME_OR_HIGHER_ROLE)
            }

            val removed = clan.removeMember(member)

            if (!removed) {
                failCommand(KICK_FAILED)
            }

            val memberName = usernameOrUuid(member.uuid)

            clan.broadcast(memberKickedMessage(memberName, player.username))
        }
    }
)
