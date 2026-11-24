package dev.slne.surf.social.minestom.clan.command.subcommands.member

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.minestom.clan.command.arguments.clanByClanTagArgument
import dev.slne.surf.social.minestom.clan.command.resolveClanArgumentOrOwn
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.command.clanMembersPagination
import dev.slne.surf.social.core.client.clan.command.collectClanMemberData
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions

private const val CLAN_NODE = "clan"

fun CommandAPICommand.clanMembersCommand(): CommandAPICommand = withSubcommand(
    subcommand("members") {
        withPermission(ClanPermissions.CLAN_VIEW_MEMBERS_COMMAND)

        clanByClanTagArgument(CLAN_NODE, optional = true)

        playerExecutorSuspend { player, args ->
            val clan: Clan = args.resolveClanArgumentOrOwn(CLAN_NODE, player.uuid)

            val data = collectClanMemberData(clan)
            val pagination = clanMembersPagination(clan as ClanImpl)

            player.sendMessage(pagination.renderComponent(data))
        }
    }
)
