package dev.slne.surf.social.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.paper.clan.commands.subcommands.options.clanTagColorCommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions

fun CommandAPICommand.clanOptionsCommand() = subcommand("options") {
    withPermission(ClanPermissions.CLAN_OPTIONS_COMMAND)

    clanTagColorCommand()
}