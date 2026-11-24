package dev.slne.surf.player.minestom.clan.command.subcommands

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.minestom.clan.command.arguments.clanByClanTagArgument
import dev.slne.surf.player.minestom.clan.command.resolveClanArgumentOrOwn
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.client.clan.components.ClanComponents
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions

private const val CLAN_TAG_NODE = "clanTag"

fun CommandAPICommand.clanInfoCommand(): CommandAPICommand = withSubcommand(
    subcommand("info") {
        withPermission(ClanPermissions.CLAN_INFO_COMMAND)

        clanByClanTagArgument(CLAN_TAG_NODE, optional = true)

        playerExecutorSuspend { player, args ->
            val clan: Clan = args.resolveClanArgumentOrOwn(CLAN_TAG_NODE, player.uuid)

            player.sendMessage(ClanComponents.renderClanInformation(clan as ClanImpl))
        }
    }
)
