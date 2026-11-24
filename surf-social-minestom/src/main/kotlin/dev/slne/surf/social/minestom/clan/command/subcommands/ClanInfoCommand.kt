package dev.slne.surf.social.minestom.clan.command.subcommands

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.minestom.clan.command.arguments.clanByClanTagArgument
import dev.slne.surf.social.minestom.clan.command.resolveClanArgumentOrOwn
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.components.ClanComponents
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions

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
